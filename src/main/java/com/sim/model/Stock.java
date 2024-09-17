package com.sim.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Data;

@Data
@Entity
public class Stock {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String symbol;  // Stock ticker symbol, e.g., AAPL, TSLA

    private String name;  // Full name of the stock

    private double price;  // Current price of the stock

    // Other details can be added later such as market capitalization, volume, etc.
}
