package com.sim.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.ui.Model;

import com.sim.model.UserDtls;
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
	
	@GetMapping("/signin")
	public String login()
	{
		return "login";
	}
	
//	@GetMapping("/register")
//	public String register()
//	{
//		return "register";
//	}
	
	@GetMapping("/register")
    public String register(Model model, HttpSession session) {
        Object msg = session.getAttribute("msg");
        if (msg != null) {
            model.addAttribute("msg", msg);
            session.removeAttribute("msg");  // Remove the message after adding it to the model
        }
        return "register";
    }
	
	@PostMapping("/createUser")
	public String createuser(@ModelAttribute UserDtls user, HttpSession session)
	{
//		System.out.println(user);
		
		boolean f = userService.checkEmail(user.getEmail());
		
		if(f)
		{
			session.setAttribute("msg", "Email id already Exist");
		} 
		else 
		{
			UserDtls userDtls = userService.createUser(user);
			if(userDtls!=null)
			{
				session.setAttribute("msg", "Register Successfully");
			} else {
				session.setAttribute("msg", "Error");
			}
		}
		
		
		return "redirect:/register";
	}
	
	
}
