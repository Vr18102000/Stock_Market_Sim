package com.sim.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.sim.model.Stock;
import com.sim.model.Transaction;
import com.sim.model.UserDtls;
import com.sim.model.UserLeaderboardDTO;
import com.sim.model.Portfolio;
import com.sim.repository.StockRepository;
import com.sim.repository.TransactionRepository;
import com.sim.repository.UserRepository;
import com.sim.repository.PortfolioRepository;

import jakarta.transaction.Transactional;
import java.util.Date;
import java.util.List;
import java.util.ArrayList;
import java.util.Comparator;

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
    
    @Autowired
    private UserRepository userRepo;
    
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
    
    @Transactional
    public boolean sellStock(String symbol, int quantity, UserDtls user) {
        // Fetch stock from the database
        Stock stock = stockRepo.findBySymbol(symbol);

        if (stock != null) {
            // Check if the user has enough stocks in their portfolio
            Portfolio portfolio = portfolioRepo.findByUserAndStock(user, stock);

            if (portfolio != null && portfolio.getQuantity() >= quantity) {
                // Update the portfolio
                portfolio.setQuantity(portfolio.getQuantity() - quantity);
                if (portfolio.getQuantity() == 0) {
                    // Remove the stock from the portfolio if quantity is zero
                    portfolioRepo.delete(portfolio);
                } else {
                    portfolioRepo.save(portfolio);
                }

                // Log the transaction
                Transaction transaction = new Transaction();
                transaction.setUser(user);
                transaction.setStock(stock);
                transaction.setQuantity(quantity);
                transaction.setPrice(stock.getPrice());
                transaction.setDate(new Date());
                transaction.setType("SELL");

                transactionRepo.save(transaction);

                return true;
            } else {
                // User doesn't have enough stocks to sell
                return false;
            }
        } else {
            // Stock doesn't exist in the repository
            return false;
        }
    }
    

    public List<UserLeaderboardDTO> getLeaderboard() {
        List<UserDtls> users = userRepo.findAll();
        List<UserLeaderboardDTO> leaderboard = new ArrayList<>();

        for (UserDtls user : users) {
            double totalPortfolioValue = 0;

            // Fetch the user's portfolio
            List<Portfolio> portfolios = portfolioRepo.findByUser(user);
            for (Portfolio portfolio : portfolios) {
                totalPortfolioValue += portfolio.getQuantity() * portfolio.getStock().getPrice();
            }

            UserLeaderboardDTO userStats = new UserLeaderboardDTO();
            userStats.setFullName(user.getFullName());
            userStats.setEmail(user.getEmail());
            userStats.setPortfolioValue(totalPortfolioValue);

            leaderboard.add(userStats);
        }

        // Sort by portfolio value in descending order
        leaderboard.sort(Comparator.comparing(UserLeaderboardDTO::getPortfolioValue).reversed());

        return leaderboard;
    }
    
}
