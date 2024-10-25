package com.sim.service;

import com.sim.model.UserDtls;

public interface UserService {
	
	
	public UserDtls createUser(UserDtls user, String url);
	
	public UserDtls findUserByResetToken(String token, String email);
	
	public boolean checkEmail(String email);
	
	public boolean verifyAccount(String code);
	
	public void removeSessionMessage();
	
	public void sendResetPasswordEmail(UserDtls user);
	
	public void increaseFailedAttempt(UserDtls user);
	
	public void resetAttempt(String email);
	
	public void lock(UserDtls user);
	
	public boolean unlockAccountTimeExpired(UserDtls user);
	
	public boolean updatePassword(String email, String newPassword, String token);
}
