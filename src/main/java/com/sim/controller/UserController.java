package com.sim.controller;

import java.security.Principal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.sim.model.Portfolio;
import com.sim.model.Transaction;
import com.sim.model.UserDtls;
import com.sim.model.UserLeaderboardDTO;
import com.sim.repository.PortfolioRepository;
import com.sim.repository.TransactionRepository;
import com.sim.repository.UserRepository;
import com.sim.service.StockService;
import com.sim.service.TradingService;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/user")
public class UserController {

//	@Autowired
//	private UserRepository userRepo;
//	
//	@Autowired
//	private TradingService tradingService;
//	
//	@Autowired
//	private BCryptPasswordEncoder passwordEncoder;
//	
//	@Autowired
//	private PortfolioRepository portfolioRepo;
//	
//	@Autowired
//	private TransactionRepository transactionRepo;
//	
//	@Autowired
//	private StockService stockService;
	
    private final UserRepository userRepo;
    private final TradingService tradingService;
    private final BCryptPasswordEncoder passwordEncoder;
    private final PortfolioRepository portfolioRepo;
    private final TransactionRepository transactionRepo;
    private final StockService stockService;

    @Autowired
    public UserController(UserRepository userRepo, TradingService tradingService, BCryptPasswordEncoder passwordEncoder,
                          PortfolioRepository portfolioRepo, TransactionRepository transactionRepo, StockService stockService) {
        this.userRepo = userRepo;
        this.tradingService = tradingService;
        this.passwordEncoder = passwordEncoder;
        this.portfolioRepo = portfolioRepo;
        this.transactionRepo = transactionRepo;
        this.stockService = stockService;
    }
	
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
	public String changePassword(Principal p, @RequestParam("oldPass") String oldPass, @RequestParam("newPass") String newPass, HttpSession session) {
	    String email = p.getName();
	    UserDtls loginUser = userRepo.findByEmail(email);

	    // Check if the old password matches
	    boolean isOldPasswordCorrect = passwordEncoder.matches(oldPass, loginUser.getPassword());
	    
	    if (!isOldPasswordCorrect) {
	        session.setAttribute("msg", "Old password is incorrect");
	        return "redirect:/user/changePass";
	    }

	    // Check new password strength
	    if (!isStrongPassword(newPass)) {
	        session.setAttribute("msg", "Password must be at least 8 characters long, contain uppercase and lowercase letters, numbers, and special characters.");
	        return "redirect:/user/changePass";
	    }

	    // Update the password
	    loginUser.setPassword(passwordEncoder.encode(newPass));
	    UserDtls updatedUser = userRepo.save(loginUser);

	    // Set session message based on success or failure
	    if (updatedUser != null) {
	        session.setAttribute("msg", "Password changed successfully");
	    } else {
	        session.setAttribute("msg", "Error occurred on the server");
	    }

	    return "redirect:/user/changePass";
	}

	// Utility method to check password strength
	private boolean isStrongPassword(String password) {
	    return password.length() >= 8 &&
	           password.matches(".*[A-Z].*") &&    // At least one uppercase letter
	           password.matches(".*[a-z].*") &&    // At least one lowercase letter
	           password.matches(".*[0-9].*") &&    // At least one digit
	           password.matches(".*[@$!%*?&#].*"); // At least one special character
	}

//	@PostMapping("/buyStock")
//	public String buyStock(@RequestParam String symbol, @RequestParam int quantity, Principal principal) {
//	    UserDtls user = userRepo.findByEmail(principal.getName());
//
//	    tradingService.buyStock(symbol, quantity, user);
//
//	    return "redirect:/user/";  // Redirect back to home
//	}
	
	@GetMapping("/searchStock")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> searchStock(@RequestParam String symbol) {
        Map<String, Object> response = new HashMap<>();
        double price = stockService.getStockPrice(symbol);  // Fetch stock price from StockService

        if (price > 0) {
            response.put("price", price);
            return ResponseEntity.ok(response);
        } else {
            response.put("error", "Stock not found");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
    }
	
	@PostMapping("/buyStock")
	public ResponseEntity<String> buyStock(@RequestBody Map<String, String> payload, Principal principal) {
	    // Fetch 'symbol' and 'quantity' from the request body
	    String symbol = payload.get("symbol");
	    int quantity = Integer.parseInt(payload.get("quantity"));
	    
	    UserDtls user = userRepo.findByEmail(principal.getName());
	    boolean success = tradingService.buyStock(symbol, quantity, user);

	    if (success) {
	        return ResponseEntity.ok("Stock purchased successfully!");
	    } else {
	        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Failed to purchase stock.");
	    }
	}

	@GetMapping("/portfolio")
	public String viewPortfolio(Model model, Principal principal) {
	    // Get the logged-in user's details
	    UserDtls user = userRepo.findByEmail(principal.getName());

	    // Fetch the user's portfolio
	    List<Portfolio> portfolios = portfolioRepo.findByUser(user);

	    // Log the fetched portfolio data for debugging (optional)
//	    portfolios.forEach(portfolio -> {
//	        System.out.println("Stock: " + portfolio.getStock().getSymbol());
//	        System.out.println("Quantity: " + portfolio.getQuantity());
//	    });

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
	
	@GetMapping("/stock")
    public String stockPage(Model model) {
		Map<String, Double> cachedStockPrices = stockService.getCachedStockPrices();  // Get cached stock prices
	    model.addAttribute("stockPrices", cachedStockPrices);  // Add cached prices to the model
        return "user/stock";  // Return the new Thymeleaf stock page
    }
	
	// Endpoint to manually push a test WebSocket message
//    @GetMapping("/test-websocket")
//    @ResponseBody
//    public String testWebSocket() {
//        stockService.sendTestStockUpdate();  // Manually send a stock update
//        return "Test WebSocket message sent!";
//    }
}
