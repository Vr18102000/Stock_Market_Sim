package com.sim.service;

import com.sim.model.UserDetails;

public interface UserService {
	
	
	public UserDetails createUser(UserDetails user);
	
	public boolean checkEmail(String email);
}
