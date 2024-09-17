package com.sim.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import com.sim.model.Stock;
import kong.unirest.HttpResponse;
import kong.unirest.JsonNode;
import kong.unirest.Unirest;

@Service
public class StockService {

    private static final Logger logger = LoggerFactory.getLogger(StockService.class);
    private static final String API_URL = "http://localhost:5000/stock/";

    public Stock getStockData(String symbol) {
        logger.info("Fetching stock data for symbol: " + symbol);

        // Make a REST call to the Python Flask API
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
}
