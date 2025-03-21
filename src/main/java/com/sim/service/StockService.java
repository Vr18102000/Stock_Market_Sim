package com.sim.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import com.sim.model.Stock;
import com.sim.repository.StockRepository;
import kong.unirest.HttpResponse;
import kong.unirest.JsonNode;
import kong.unirest.Unirest;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Service
public class StockService {

    private static final Logger logger = LoggerFactory.getLogger(StockService.class);
//    private static final String API_URL = "https://stockmarketsim-api.onrender.com/stock/";
    private static final String API_URL = "https://stockserv-evaggva0anapcrcw.canadacentral-01.azurewebsites.net/stock/";
//    private static final String API_URL = "http://localhost:5000/stock/";

    private Map<String, Double> stockCache = new HashMap<>();

    private final SimpMessagingTemplate messagingTemplate;
    private final StockRepository stockRepository;
    private ScheduledExecutorService scheduler;

    public StockService(SimpMessagingTemplate messagingTemplate, StockRepository stockRepository) {
        this.messagingTemplate = messagingTemplate;
        this.stockRepository = stockRepository;
    }

    // Method to start fetching stock updates
    public void startFetchingData() {
        if (scheduler == null || scheduler.isShutdown()) {
            scheduler = Executors.newSingleThreadScheduledExecutor();
            scheduler.scheduleAtFixedRate(this::fetchStockUpdates, 0, 60, TimeUnit.SECONDS);
            logger.info("Started fetching stock updates after user login.");
        }
    }

    // Method to stop fetching stock updates
    public void stopFetchingData() {
        if (scheduler != null && !scheduler.isShutdown()) {
            scheduler.shutdown();
            logger.info("Stopped fetching stock updates.");
        }
    }

    // Fetch stock data every 60 seconds (called by the scheduler)
    public void fetchStockUpdates() {
        logger.info("Fetching stock updates...");

        List<Stock> stocks = stockRepository.findAll();

        for (Stock stock : stocks) {
            Stock updatedStock = getStockData(stock.getSymbol());
            if (updatedStock != null) {
                stockCache.put(stock.getSymbol(), updatedStock.getPrice());

                // Push updated stock data to WebSocket clients
                messagingTemplate.convertAndSend("/topic/stockPrices", updatedStock);
                logger.info("Pushed stock update to WebSocket: " + updatedStock);
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

            logger.info("Stock data: " + stock);
            return stock;
        } else {
            logger.error("Failed to fetch stock data. Status: " + response.getStatus());
            return null;
        }
    }
    
    // Method to get the price of a specific stock symbol, with cache support
    public double getStockPrice(String symbol) {
        // Check if the stock price is already cached
        if (stockCache.containsKey(symbol)) {
            return stockCache.get(symbol);
        }
        
        // If not cached, fetch the price from the API and store it in the cache
        Stock stock = getStockData(symbol);
        if (stock != null) {
            stockCache.put(symbol, stock.getPrice());  // Cache the fetched price
            return stock.getPrice();
        }

        return -1;  // Return -1 if the stock is not found
    }

    // Get cached stock prices for immediate use on the stock page
    public Map<String, Double> getCachedStockPrices() {
        return stockCache;
    }
}
