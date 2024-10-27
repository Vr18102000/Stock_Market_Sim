package com.sim.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.security.authentication.event.LogoutSuccessEvent;
import org.springframework.stereotype.Component;

@Component
public class LogoutListener {

    private static final Logger logger = LoggerFactory.getLogger(LogoutListener.class);

    private final StockService stockService;

    @Autowired
    public LogoutListener(StockService stockService) {
        this.stockService = stockService;
    }

    @EventListener
    public void onApplicationEvent(LogoutSuccessEvent event) {
        logger.info("User logout detected, stopping stock data fetch...");
        stockService.stopFetchingData();
    }
}