package com.sim.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.sim.model.UserDtls;

public interface UserRepository extends JpaRepository<UserDtls, Integer>{

	public boolean existsByEmail(String email);
	
	public UserDtls findByEmail(String email);
	
}
