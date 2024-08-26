package com.sim.controller;

import java.security.Principal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.sim.model.UserDtls;
import com.sim.repository.UserRepository;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/user")
public class UserController {

	@Autowired
	private UserRepository userRepo;
	
	@Autowired
	private BCryptPasswordEncoder passwordEncode;
	
	@ModelAttribute
	private void userDetails(Model m, Principal p) {
		String email = p.getName();
		UserDtls user = userRepo.findByEmail(email);
		m.addAttribute("user", user);
		
	}
	
	@GetMapping("/")
	public String home()		
	{
		return "user/home";
	}
	
//	@GetMapping("/changePass")
//	public String loadChangePassword()		
//	{
//		return "user/change_password";
//	}
	
	@GetMapping("/changePass")
    public String register(Model model, HttpSession session) {
        Object msg = session.getAttribute("msg");
        if (msg != null) {
            model.addAttribute("msg", msg);
            session.removeAttribute("msg");  // Remove the message after adding it to the model
        }
        return "user/change_password";
	}
	
	@PostMapping("/updatePassword")
	public String changePassword(Principal p, @RequestParam("oldPass") String oldPass, @RequestParam("newPass") String newPass, HttpSession session)
	{
		String email = p.getName();
		UserDtls loginUser = userRepo.findByEmail(email);
		
		boolean f = passwordEncode.matches(oldPass, loginUser.getPassword());
		
		if(f)
		{
			loginUser.setPassword(passwordEncode.encode(newPass));
			UserDtls updatePasswordUser = userRepo.save(loginUser);
			
			if(updatePasswordUser != null)
			{
				session.setAttribute("msg", "Password Change Successfully");
			} else {
				session.setAttribute("msg", "Something wrong on server");
			}
		
		} else {
			session.setAttribute("msg", "Old password is incorrect");
		}
		return "redirect:/user/changePass";
	}
	
}
