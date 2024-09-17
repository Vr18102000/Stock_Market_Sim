package com.sim.model;

import java.util.Date;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import lombok.Data;

@Data
@Entity
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private UserDtls user;  // Reference to the user who made the transaction

    @ManyToOne
    private Stock stock;  // Reference to the stock involved in the transaction

    private int quantity;  // Number of shares bought/sold

    private double price;  // Price per share at the time of the transaction

    private Date date;  // Date of the transaction

    private String type;  // "BUY" or "SELL"
}
