package com.sim.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.stereotype.Component;

@Component
public class LoginListener {

    private static final Logger logger = LoggerFactory.getLogger(LoginListener.class);

    private final StockService stockService;

    @Autowired
    public LoginListener(StockService stockService) {
        this.stockService = stockService;
    }

    @EventListener
    public void onApplicationEvent(AuthenticationSuccessEvent event) {
        logger.info("User login detected, starting stock data fetch...");
        stockService.startFetchingData();
    }
}
