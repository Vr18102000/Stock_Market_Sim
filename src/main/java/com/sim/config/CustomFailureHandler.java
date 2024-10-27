package com.sim.config;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import com.sim.model.UserDtls;
import com.sim.repository.UserRepository;
import com.sim.service.UserService;
import com.sim.service.UserServiceImpl;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class CustomFailureHandler  extends SimpleUrlAuthenticationFailureHandler{

	@Autowired
	@Lazy
	private UserService userService;
	
	@Autowired
	private UserRepository userRepo;
	
	@Override
	public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
			AuthenticationException exception) throws IOException, ServletException {
		
		String email = request.getParameter("username");
		UserDtls user = userRepo.findByEmail(email);
		
		if(user!=null)
		{
			if(user.isEnabled())
			{
				if(user.isAccountNonLocked())
				{
					if(user.getFailedAttempt()<UserServiceImpl.ATTEMPT_TIME - 1)
					{
						userService.increaseFailedAttempt(user);
					} else {
						userService.lock(user);
						exception = new LockedException("Your account is locked! Please try again in 10 minutes");
					}
				} else if(!user.isAccountNonLocked())
				{
					if(userService.unlockAccountTimeExpired(user))
					{
						exception = new LockedException("Account is unlocked! Please try to login");
					} else {
						exception = new LockedException("Account is locked! Please try again after sometime");
					}
				}
				
				
			}else {
				exception= new LockedException("You account is inactive. Verify the account through the link sent on your email");
			}
		}
		
		super.setDefaultFailureUrl("/?error");
		super.onAuthenticationFailure(request, response, exception);
	}
	
}
