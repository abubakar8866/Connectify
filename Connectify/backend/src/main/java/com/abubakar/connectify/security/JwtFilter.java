package com.abubakar.connectify.security;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.abubakar.connectify.entity.User;
import com.abubakar.connectify.exception.EmailNotFound;
import com.abubakar.connectify.exception.IncorrectCredentialsException;
import com.abubakar.connectify.service.impl.CustomUserDetailsService;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class JwtFilter extends OncePerRequestFilter {
	
	@Autowired
	private JwtUtils jwtUtils;
	
	@Autowired
	private CustomUserDetailsService customUserDetailsService;
	
	private static final Logger logger = LoggerFactory.getLogger(JwtFilter.class);
	
	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
	        FilterChain filterChain) throws ServletException, IOException {

	    final String authHeader = request.getHeader("Authorization");
	    final String jwtToken;
	    final String email;

	    // Check Authorization Header
	    if (authHeader == null || !authHeader.startsWith("Bearer ")) {
	        logger.warn("Authorization header missing or invalid");
	        filterChain.doFilter(request, response);
	        return;
	    }

	    // Extract JWT Token
	    jwtToken = authHeader.substring(7);

	    // Extract Email from Token
	    email = jwtUtils.extractEmail(jwtToken);

	    // Check Authentication
	    if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {

	        User user = (User) this.customUserDetailsService.loadUserByUsername(email);

	        // Validate Token
	        if (this.jwtUtils.isTokenValid(jwtToken, user)) {

	            UsernamePasswordAuthenticationToken authenticationToken =
	                    new UsernamePasswordAuthenticationToken(
	                            user,
	                            null,
	                            user.getAuthorities());

	            authenticationToken.setDetails(
	                    new WebAuthenticationDetailsSource().buildDetails(request));

	            SecurityContextHolder.getContext().setAuthentication(authenticationToken);

	            logger.info("User authenticated successfully: {}", email);

	        } else {
	            logger.error("Invalid JWT token for user: {}", email);
	            throw new IncorrectCredentialsException("Invalid JWT token for user: "+ email);
	        }

	    } else {

	        if (email == null) {
	            logger.error("Email extraction from JWT token failed");
	            throw new EmailNotFound("Email extraction from JWT token failed");
	        }

	        if (SecurityContextHolder.getContext().getAuthentication() != null) {
	            logger.warn("User already authenticated");
	        }
	    }

	    filterChain.doFilter(request, response);
	}	
	
}
