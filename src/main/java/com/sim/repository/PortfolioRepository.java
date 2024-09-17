package com.sim.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import com.sim.model.Portfolio;
import com.sim.model.Stock;
import com.sim.model.UserDtls;

public interface PortfolioRepository extends JpaRepository<Portfolio, Long> {
    
    // Find all portfolio records for a given user
    List<Portfolio> findByUser(UserDtls user);
    
    // Find a specific stock in the user's portfolio
    Portfolio findByUserAndStock(UserDtls user, Stock stock);
}
