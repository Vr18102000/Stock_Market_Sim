package com.sim.service;

import java.util.Date;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.sim.model.UserDtls;
import com.sim.repository.UserRepository;

import jakarta.mail.internet.MimeMessage;
import jakarta.servlet.http.HttpSession;
import jakarta.transaction.Transactional;
import net.bytebuddy.utility.RandomString;

@Transactional
@Service
public class UserServiceImpl implements UserService{

	@Autowired
	private UserRepository userRepo;
	
	@Autowired
	private BCryptPasswordEncoder passwordEncoder;
	
	@Autowired
	private JavaMailSender mailSender;
	
	@Override
	public UserDtls createUser(UserDtls user, String url) {
		
		user.setPassword(passwordEncoder.encode(user.getPassword()));
		user.setRole("ROLE_USER");
		
		user.setEnabled(false);
		RandomString rn = new RandomString();
		user.setVerificationCode(rn.make(64));
		
		user.setAccountNonLocked(true);
		user.setFailedAttempt(0);
		user.setLockTime(null);
		
		UserDtls us = userRepo.save(user);
		
		if(us !=null)
		{
			sendVerificationMail(user, url);
		}
		return us;
	}

	@Override
	public boolean checkEmail(String email) {
		
		return userRepo.existsByEmail(email);
	}
		
	public void sendVerificationMail(UserDtls user, String url)
	{
		String from = "vsucker0@gmail.com";
		String to = user.getEmail();
		String subject = "Account Verification";
		String content = "Dear [[name]],<br>"
				+ "Please click the link given below to verify your registration:<br>"
				+ "<h3><a href=\"[[URL]]\" target=\"_self\">Verify</a></h3>"
				+ "Thank you,<br>"
				+ "Vr18";
		
		try {
			MimeMessage message = mailSender.createMimeMessage();
			MimeMessageHelper helper =  new MimeMessageHelper(message);
			helper.setFrom(from, "Vishal");
			helper.setTo(to);
			helper.setSubject(subject);
			
			content = content.replace("[[name]]", user.getFullName());
			
			String siteUrl = url+"/verify?code="+user.getVerificationCode();
			content = content.replace("[[URL]]", siteUrl);
			
			helper.setText(content, true);
			mailSender.send(message);
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}

	@Override
	public boolean verifyAccount(String code) {
		
		UserDtls user = userRepo.findByVerificationCode(code);
		if(user != null)
		{
			user.setEnabled(true);
			user.setVerificationCode(null);
			userRepo.save(user);
			return true;
		}
		return false;
	}

	@Override
	public void removeSessionMessage() {
		
		HttpSession session = ((ServletRequestAttributes)(RequestContextHolder.getRequestAttributes())).getRequest()
								.getSession();
		session.removeAttribute("msg");
		
	}

	@Override
	public void increaseFailedAttempt(UserDtls user) {
		int attempt = user.getFailedAttempt()+1;
		userRepo.updateFailedAttempt(attempt, user.getEmail());
	}

	//Time is in milliseconds
	private static final long lock_duration_time = 600000;
	
	public static final long ATTEMPT_TIME = 3;
	
	@Override
	public void resetAttempt(String email) {
		userRepo.updateFailedAttempt(0, email);
	}

	@Override
	public void lock(UserDtls user) {
		user.setAccountNonLocked(false);
		user.setLockTime(new Date());
		userRepo.save(user);
	}

	@Override
	public boolean unlockAccountTimeExpired(UserDtls user) {
		
		long lockTimeInMills = user.getLockTime().getTime();
		long currentTimeMillis = System.currentTimeMillis();
		
		if(lockTimeInMills+lock_duration_time < currentTimeMillis)
		{
			user.setAccountNonLocked(true);
			user.setLockTime(null);
			user.setFailedAttempt(0);
			userRepo.save(user);
			return true;
		}
		return false;
	}
	
	@Override
    public void sendResetPasswordEmail(UserDtls user) {
        String token = UUID.randomUUID().toString();
        user.setResetToken(token);
        userRepo.save(user);

        String resetLink = "http://localhost:8080/resetPasswordForm?token=" + token + "&email=" + user.getEmail();
        String subject = "Password Reset Request";
        String content = "Dear [[name]],<br>"
                + "Click the link below to reset your password:<br>"
                + "<h3><a href=\"[[URL]]\" target=\"_self\">Reset Password</a></h3>"
                + "Ignore this email if you didn't request a password reset.";

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message);

            helper.setFrom("your-email@example.com");
            helper.setTo(user.getEmail());
            helper.setSubject(subject);

            content = content.replace("[[name]]", user.getFullName());
            content = content.replace("[[URL]]", resetLink);

            helper.setText(content, true);

            mailSender.send(message);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public UserDtls findUserByResetToken(String token, String email) {
        return userRepo.findByResetTokenAndEmail(token, email);
    }

    @Override
    public boolean updatePassword(String email, String newPassword, String token) {
        UserDtls user = userRepo.findByResetTokenAndEmail(token, email);
        if (user != null && user.getResetToken().equals(token)) {
            user.setPassword(passwordEncoder.encode(newPassword));
            user.setResetToken(null);  // Clear the token
            userRepo.save(user);
            return true;
        }
        return false;
    }
}

