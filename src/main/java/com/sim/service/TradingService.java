package com.sim.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.sim.model.Stock;
import com.sim.model.Transaction;
import com.sim.model.UserDtls;
import com.sim.model.Portfolio;
import com.sim.repository.StockRepository;
import com.sim.repository.TransactionRepository;
import com.sim.repository.PortfolioRepository;

import jakarta.transaction.Transactional;
import java.util.Date;

@Service
public class TradingService {

    private static final Logger logger = LoggerFactory.getLogger(TradingService.class);

    @Autowired
    private StockRepository stockRepo;

    @Autowired
    private TransactionRepository transactionRepo;

    @Autowired
    private PortfolioRepository portfolioRepo;  // New portfolio repository

    @Autowired
    private StockService stockService;

    @Transactional
    public boolean buyStock(String symbol, int quantity, UserDtls user) {
        logger.info("Buying stock: Symbol = " + symbol + ", Quantity = " + quantity);

        // Get stock data from Yahoo Finance API
        Stock stock = stockService.getStockData(symbol);

        if (stock != null) {
            stockRepo.save(stock);  // Save stock data to the database
            logger.info("Stock saved: " + stock.toString());

            // Update the user's portfolio
            Portfolio portfolio = portfolioRepo.findByUserAndStock(user, stock);

            if (portfolio == null) {
                // Create a new portfolio entry if the user doesn't own this stock yet
                portfolio = new Portfolio();
                portfolio.setUser(user);
                portfolio.setStock(stock);
                portfolio.setQuantity(quantity);
            } else {
                // Update the quantity if the user already owns the stock
                portfolio.setQuantity(portfolio.getQuantity() + quantity);
            }

            portfolioRepo.save(portfolio);  // Save portfolio update
            logger.info("Portfolio updated: " + portfolio.toString());

            // Create a new transaction
            Transaction transaction = new Transaction();
            transaction.setUser(user);
            transaction.setStock(stock);
            transaction.setQuantity(quantity);
            transaction.setPrice(stock.getPrice());
            transaction.setDate(new Date());
            transaction.setType("BUY");

            transactionRepo.save(transaction);  // Save the transaction
            logger.info("Transaction saved: " + transaction.toString());

            return true;
        } else {
            logger.error("Failed to fetch stock data for symbol: " + symbol);
            return false;
        }
    }
}
