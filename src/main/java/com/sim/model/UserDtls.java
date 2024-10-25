package com.sim.model;


import java.util.Date;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

@Data
@Entity
public class UserDtls {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private int id;
	
	private String fullName;
	
	@NotEmpty(message = "Email is required")
	@Email(message = "Please provide a valid email address")
	private String email;
	
	private String address;
	
	private String mobileNumber;
	
	private String password;
	
	private String role;
	
	private boolean accountNonLocked;
	
	private int failedAttempt;
	
	private Date lockTime;
	
	private boolean enabled;
	
	private String verificationCode;
	
	private String resetToken;
	
	private Date tokenExpiry;
}
