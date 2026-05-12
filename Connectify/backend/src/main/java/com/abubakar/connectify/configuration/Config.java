package com.abubakar.connectify.configuration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.abubakar.connectify.security.JwtFilter;
import com.abubakar.connectify.security.OAuth2SuccessHandler;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class Config {
	
	@Autowired
	private JwtFilter jwtFilter;
	
	@Autowired
	private AuthenticationProvider authenticationProvider;
	
	@Autowired
	private OAuth2SuccessHandler successHandler;

	@Bean
	public SecurityFilterChain filterChain(HttpSecurity httpSecurity) throws Exception {
		httpSecurity.csrf(AbstractHttpConfigurer::disable)
			.authorizeHttpRequests(auth -> auth
							.requestMatchers(
									"/api/v1/auth/register",
									"/api/v1/auth/login",
									"/api/v1/auth/forgot-password",
									"/api/v1/auth/reset-password",
									"/api/v1/auth/create-admin",
									"/oauth2/**",
									"/login/**",
									"/swagger-ui/**"
							).permitAll()
		            .anyRequest().authenticated()
					)
			.oauth2Login(oauth -> oauth
					.successHandler(successHandler)
					)
			.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
			.authenticationProvider(authenticationProvider)
			.addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
			;
		return httpSecurity.build();
	}
	
}
