package com.sim.config;

import java.io.IOException;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

import com.sim.model.UserDtls;
import com.sim.service.UserService;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Configuration
public class CustomSuccessHandler implements AuthenticationSuccessHandler {

	@Autowired
	private UserService userService;
	
	@Override
	public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
			Authentication authentication) throws IOException, ServletException {
		
		Set<String> roles = AuthorityUtils.authorityListToSet(authentication.getAuthorities());
		
		CustomUserDetails customUserDetails = (CustomUserDetails)authentication.getPrincipal();
		UserDtls user = customUserDetails.getUser();
		
		if(user !=null)
		{
			userService.resetAttempt(user.getEmail());
		}
		
		if(roles.contains("ROLE_ADMIN"))
		{
			response.sendRedirect("/admin/");
		}  
		else 
		{
			response.sendRedirect("/user/");
		}
		
	}
	
}
