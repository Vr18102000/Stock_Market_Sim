package com.sim.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.sim.model.UserDetails;

public interface UserRepository extends JpaRepository<UserDetails, Integer>{

	public boolean existsByEmail(String email);
}
