package com.sim.controller;

import java.security.Principal;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.sim.model.Portfolio;
import com.sim.model.Transaction;
import com.sim.model.UserDtls;
import com.sim.model.UserLeaderboardDTO;
import com.sim.repository.PortfolioRepository;
import com.sim.repository.TransactionRepository;
import com.sim.repository.UserRepository;
import com.sim.service.TradingService;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/user")
public class UserController {

	@Autowired
	private UserRepository userRepo;
	
	@Autowired
	private TradingService tradingService;
	
	@Autowired
	private BCryptPasswordEncoder passwordEncode;
	
	@Autowired
	private PortfolioRepository portfolioRepo;
	
	@Autowired
	private TransactionRepository transactionRepo;
	
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

	@PostMapping("/buyStock")
	public String buyStock(@RequestParam String symbol, @RequestParam int quantity, Principal principal) {
	    UserDtls user = userRepo.findByEmail(principal.getName());

	    tradingService.buyStock(symbol, quantity, user);

	    return "redirect:/user/";  // Redirect back to home
	}

	@GetMapping("/portfolio")
	public String viewPortfolio(Model model, Principal principal) {
	    // Get the logged-in user's details
	    UserDtls user = userRepo.findByEmail(principal.getName());

	    // Fetch the user's portfolio
	    List<Portfolio> portfolios = portfolioRepo.findByUser(user);

	    // Log the fetched portfolio data for debugging (optional)
	    portfolios.forEach(portfolio -> {
	        System.out.println("Stock: " + portfolio.getStock().getSymbol());
	        System.out.println("Quantity: " + portfolio.getQuantity());
	    });

	    // Add the portfolio data to the model
	    model.addAttribute("portfolios", portfolios);

	    return "user/portfolio";  // Return the new portfolio page
	}

	@GetMapping("/user/transactions")
	public String transactionHistory(Model model, Principal principal) {
	    UserDtls user = userRepo.findByEmail(principal.getName());

	    // Fetch the user's transaction history
	    List<Transaction> transactions = transactionRepo.findByUser(user);
	    model.addAttribute("transactions", transactions);

	    return "user/transaction_history";
	}

	@PostMapping("/sellStock")
	public String sellStock(@RequestParam String symbol, @RequestParam int quantity, Principal principal, HttpSession session) {
	    UserDtls user = userRepo.findByEmail(principal.getName());

	    boolean result = tradingService.sellStock(symbol, quantity, user);
	    if (result) {
	        session.setAttribute("msg", "Stock sold successfully!");
	    } else {
	        session.setAttribute("msg", "Failed to sell stock. Check your portfolio or stock quantity.");
	    }

	    return "redirect:/user/";  // Redirect back to home or portfolio page
	}
	
	@GetMapping("/leaderboard")
	public String viewLeaderboard(Model model) {
	    List<UserLeaderboardDTO> leaderboard = tradingService.getLeaderboard();
	    model.addAttribute("leaderboard", leaderboard);
	    return "user/leaderboard";  // Create this HTML page to display the leaderboard
	}

	
}
