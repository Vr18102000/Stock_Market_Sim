package com.sim.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.sim.model.Transaction;
import com.sim.model.UserDtls;
import java.util.List;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    // Fetch all transactions for a user
    List<Transaction> findByUser(UserDtls user);
}
