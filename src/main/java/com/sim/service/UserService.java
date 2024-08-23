package com.sim.service;

import com.sim.model.UserDtls;

public interface UserService {
	
	
	public UserDtls createUser(UserDtls user);
	
	public boolean checkEmail(String email);
}
