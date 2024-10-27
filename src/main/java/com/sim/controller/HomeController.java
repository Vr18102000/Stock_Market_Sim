package com.sim.controller;

import java.security.Principal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.query.Param;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.sim.model.UserDtls;
import com.sim.repository.UserRepository;
import com.sim.service.UserService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

@Controller
public class HomeController {
	
	@Autowired
//	private UserService userService;
//	
//	@Autowired
//	private UserRepository userRepo;
//	
//	@Autowired
//	private BCryptPasswordEncoder passwordEncoder;
	
    private final UserService userService;
    private final UserRepository userRepo;
    private final BCryptPasswordEncoder passwordEncoder;

    @Autowired
    public HomeController(UserService userService, UserRepository userRepo, BCryptPasswordEncoder passwordEncoder) {
        this.userService = userService;
        this.userRepo = userRepo;
        this.passwordEncoder = passwordEncoder;
    }
	
	@ModelAttribute
	private void userDetails(Model m, Principal p) {
		if(p!=null)
		{
			String email = p.getName();
			UserDtls user = userRepo.findByEmail(email);
			m.addAttribute("user", user);
		}
	}	
	
	
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
	public String createuser(@ModelAttribute UserDtls user, HttpSession session, HttpServletRequest request)
	{
		
		String url = request.getRequestURL().toString();
		// http://localhost:8080/createUser
		url = url.replace(request.getServletPath(), "");
		
		boolean f = userService.checkEmail(user.getEmail());
		
		if(f)
		{
			session.setAttribute("msg", "Email id already Exist");
		} 
		else 
		{
			UserDtls userDtls = userService.createUser(user, url);
			if(userDtls!=null)
			{
				session.setAttribute("msg", "Register Successfully");
			} else {
				session.setAttribute("msg", "Error");
			}
		}
//		return "redirect:/register";
		return "redirect:/";
	}
	
	@GetMapping("/verify")
	public String verifyAccount(@Param("code") String code, HttpSession session)
	{
		if(userService.verifyAccount(code))
		{
			session.setAttribute("msg", "Account verified successfully! Please log in.");
	        return "redirect:/";
		} else {
			session.setAttribute("msg", "Invalid or expired verification link.");
	        return "redirect:/";
		}

	}
	
	@GetMapping("/loadForgotPassword")
	public String loadForgotPassword()
	{
		return "forgot_password";
	}
	
	@GetMapping("/loadResetPassword/{id}")
	public String loadResetPassword(@PathVariable int id, Model m)
	{
		m.addAttribute("id", id);
		return "reset_password";
	}
	
	@PostMapping("/forgotPassword")
	public String forgotPassword(@RequestParam String email, @RequestParam String mobileNumber, HttpSession session) {
        UserDtls user = userRepo.findByEmailAndMobileNumber(email, mobileNumber);
        if (user != null) {
//        	return "redirect:/loadResetPassword/" + user.getId();
            userService.sendResetPasswordEmail(user);  // New service to send the reset link
            session.setAttribute("msg", "Password reset link has been sent to your email");
        } else {
            session.setAttribute("msg", "Invalid email & mobile number");
        }
        return "redirect:/";
    }
	
	@GetMapping("/resetPasswordForm")
    public String resetPasswordForm(@RequestParam("token") String token, @RequestParam("email") String email, Model model, HttpSession session) {
        UserDtls user = userService.findUserByResetToken(token, email);
        if (user == null) {
            session.setAttribute("msg", "Invalid or expired reset token");
            return "redirect:/";
        }
        model.addAttribute("token", token);
        model.addAttribute("email", email);
        return "reset_password";
    }
	
	@PostMapping("/changePassword")
	public String resetPassword(@RequestParam String psw, @RequestParam String cpsw, @RequestParam String email, @RequestParam String token, HttpSession session) {
        if (!psw.equals(cpsw)) {
            session.setAttribute("msg", "Passwords do not match.");
            return "redirect:/resetPasswordForm?token=" + token + "&email=" + email;
        }

        if (!psw.matches("(?=.*\\d)(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=]).{8,}")) {
            session.setAttribute("msg", "Password must meet the required pattern.");
            return "redirect:/resetPasswordForm?token=" + token + "&email=" + email;
        }

        boolean isChanged = userService.updatePassword(email, psw, token);
        if (isChanged) {
            session.setAttribute("msg", "Password changed successfully!");
            return "redirect:/";
        } else {
            session.setAttribute("msg", "Invalid token or expired.");
            return "redirect:/";
        }
    }
	
}