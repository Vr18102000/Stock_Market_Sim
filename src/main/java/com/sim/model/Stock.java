package com.sim.model;

import jakarta.persistence.*;
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