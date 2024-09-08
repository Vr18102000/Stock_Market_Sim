package com.sim.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import com.sim.model.UserDtls;

public interface UserRepository extends JpaRepository<UserDtls, Integer>{

	public boolean existsByEmail(String email);
	
	public UserDtls findByEmail(String email);

	public UserDtls findByEmailAndMobileNumber(String email, String mobileNumber);
	
	public UserDtls findByVerificationCode(String code);
	
	@Query("update UserDtls user set user.failedAttempt=?1 where email=?2")
	@Modifying
	public void updateFailedAttempt(int attempt, String email);
}
