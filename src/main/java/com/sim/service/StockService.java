package com.sim.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import com.sim.model.Stock;
import com.sim.repository.StockRepository;

import kong.unirest.HttpResponse;
import kong.unirest.JsonNode;
import kong.unirest.Unirest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class StockService {

    private static final Logger logger = LoggerFactory.getLogger(StockService.class);
    private static final String API_URL = "http://localhost:5000/stock/";

    // Cache to store stock prices
    private Map<String, Double> stockCache = new HashMap<>();

    private final SimpMessagingTemplate messagingTemplate;
    private final StockRepository stockRepository;  // Inject stock repository

    public StockService(SimpMessagingTemplate messagingTemplate, StockRepository stockRepository) {
        this.messagingTemplate = messagingTemplate;
        this.stockRepository = stockRepository;  // Inject repository
    }

    // Scheduled task to fetch stock data every 60 seconds
    @Scheduled(fixedRate = 60000)
    public void fetchStockUpdates() {
        logger.info("Scheduled task triggered: Fetching stock updates...");
        
        // Fetch all stocks from the database
        List<Stock> stocks = stockRepository.findAll();

        // Fetch stock prices for each stock symbol
        for (Stock stock : stocks) {
            Stock updatedStock = getStockData(stock.getSymbol());  // Fetch stock data from Flask API
            if (updatedStock != null) {
                stockCache.put(stock.getSymbol(), updatedStock.getPrice());  // Cache the updated price

                // Push updated stock data to WebSocket clients
                messagingTemplate.convertAndSend("/topic/stockPrices", updatedStock);
                logger.info("Pushed stock update to WebSocket: " + updatedStock.toString());
            } else {
                logger.error("Failed to fetch stock data for symbol: " + stock.getSymbol());
            }
        }
    }

    // Fetch stock data from the Python Flask API
    public Stock getStockData(String symbol) {
        logger.info("Fetching stock data for symbol: " + symbol);

        HttpResponse<JsonNode> response = Unirest.get(API_URL + symbol).asJson();

        if (response.getStatus() == 200) {
            double price = response.getBody().getObject().getDouble("price");

            Stock stock = new Stock();
            stock.setSymbol(symbol);
            stock.setPrice(price);

            logger.info("Stock data: " + stock.toString());
            return stock;
        } else {
            logger.error("Failed to fetch stock data. Status: " + response.getStatus());
            return null;
        }
    }

    // Method to return cached stock prices (for immediate use on the stock page)
    public Map<String, Double> getCachedStockPrices() {
        return stockCache;  // Return the cached stock prices
    }
}
