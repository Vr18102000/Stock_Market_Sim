package com.sim.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import com.sim.model.UserDetails;
import com.sim.service.UserService;

import jakarta.servlet.http.HttpSession;

@Controller
public class HomeController {
	
	@Autowired
	private UserService userService;
	
	
	@GetMapping("/")
	public String index()
	{
		return "index";
	}
	
	@GetMapping("/login")
	public String login()
	{
		return "login";
	}
	
	@GetMapping("/register")
	public String register()
	{
		return "register";
	}
	
	@PostMapping("/createUser")
	public String createuser(@ModelAttribute UserDetails user, HttpSession session)
	{
//		System.out.println(user);
		
		boolean f=userService.checkEmail(user.getEmail());
		
		if(f)
		{
			session.setAttribute("msg", "Email id already Exist");
		} 
		else 
		{
			UserDetails userDetails = userService.createUser(user);
			if(userDetails!=null)
			{
				session.setAttribute("msg", "Register Successfully");
			} else {
				session.setAttribute("msg", "Error");
			}
		}
		
		
		return "redirect:/register";
	}
	
	
}
