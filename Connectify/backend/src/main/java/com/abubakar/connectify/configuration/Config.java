package com.abubakar.connectify.configuration;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationProvider;

import org.springframework.security.config.Customizer;
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
	public SecurityFilterChain filterChain(
			HttpSecurity httpSecurity
	) throws Exception {

		httpSecurity

				// ================= DISABLE CSRF =================

				.cors(Customizer.withDefaults())

				.csrf(AbstractHttpConfigurer::disable)

				// ================= AUTHORIZATION =================

				.authorizeHttpRequests(auth -> auth

						.requestMatchers(

								"/api/v1/auth/register",
								"/api/v1/auth/login",
								"/api/v1/auth/forgot-password",
								"/api/v1/auth/reset-password",
								"/api/v1/auth/refresh-token",
								"/api/v1/auth/logout",
								"/api/v1/auth/create-admin",
								"/uploads/**",
								"/oauth2/**",
								"/login/**",
								"/swagger-ui/**",
								"/v3/api-docs/**"
						).permitAll()

						// PUBLIC POSTS (FEED)
						.requestMatchers(HttpMethod.GET, "/api/v1/posts/**").permitAll()
						.requestMatchers(HttpMethod.GET, "/api/v1/comments/**").permitAll()
						.requestMatchers(HttpMethod.GET, "/api/v1/follows/**").permitAll()
						.requestMatchers(HttpMethod.GET, "/api/v1/likes/**").permitAll()

						// ADMIN ONLY
						.requestMatchers("/api/v1/admin/**").hasRole("ADMIN")

						// EVERYTHING ELSE
						.anyRequest().authenticated()
				)

				// ================= OAUTH2 =================

				.oauth2Login(oauth -> oauth
						.successHandler(successHandler)
				)

				// ================= STATELESS =================

				.sessionManagement(session ->
						session.sessionCreationPolicy(
								SessionCreationPolicy.STATELESS
						)
				)

				// ================= AUTH PROVIDER =================

				.authenticationProvider(authenticationProvider)

				// ================= JWT FILTER =================

				.addFilterBefore(
						jwtFilter,
						UsernamePasswordAuthenticationFilter.class
				);

		return httpSecurity.build();
	}

}

