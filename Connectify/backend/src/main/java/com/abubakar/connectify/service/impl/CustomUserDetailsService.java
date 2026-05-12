package com.abubakar.connectify.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.abubakar.connectify.entity.User;
import com.abubakar.connectify.exception.UsernameNotFound;
import com.abubakar.connectify.repository.UserRepository;

@Service
public class CustomUserDetailsService implements UserDetailsService {
	
	@Autowired
	private UserRepository userRepo;
	
	private static final Logger logger = LoggerFactory.getLogger(CustomUserDetailsService.class);

	@Override
	public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
		
		logger.info("Authentication request received for email: {}", email);

	    User user = this.userRepo.findByEmail(email).orElseThrow(() -> {
	        logger.warn("User not found with email: {}", email);
	        return new UsernameNotFound(email + " is not found.");
	    });

	    logger.debug("User loaded successfully with email: {} and role: {}", 
	            user.getEmail(), user.getRole());

	    return user;
	    
	}
	
}
