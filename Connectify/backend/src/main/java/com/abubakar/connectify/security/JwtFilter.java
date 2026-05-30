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

	private static final Logger logger =
			LoggerFactory.getLogger(JwtFilter.class);

	@Override
	protected void doFilterInternal(
			HttpServletRequest request,
			HttpServletResponse response,
			FilterChain filterChain
	) throws ServletException, IOException {

		try {

			final String authHeader =
					request.getHeader("Authorization");

			// ================= NO TOKEN =================

			if (
					authHeader == null ||
							!authHeader.startsWith("Bearer ")
			) {

				filterChain.doFilter(request, response);
				return;
			}

			// ================= EXTRACT TOKEN =================

			String jwtToken =
					authHeader.substring(7);

			// ================= EXTRACT EMAIL =================

			String email =
					jwtUtils.extractEmail(jwtToken);

			// ================= AUTHENTICATION =================

			if (
					email != null &&
							SecurityContextHolder
									.getContext()
									.getAuthentication() == null
			) {

				User user =
						(User) customUserDetailsService
								.loadUserByUsername(email);

				// ================= VALIDATE TOKEN =================

				if (!jwtUtils.isTokenValid(jwtToken, user)) {

					logger.error(
							"Invalid JWT token for user: {}",
							email
					);

					response.setStatus(
							HttpServletResponse.SC_UNAUTHORIZED
					);

					response.setContentType(
							"application/json"
					);

					response.getWriter().write(
							"""
                            {
                                "message":"Invalid JWT token",
                                "success":false,
                                "status":401,
                                "errorCode":"INVALID_JWT",
                                "timestamp":%d
                            }
                            """.formatted(
									System.currentTimeMillis()
							)
					);

					return;
				}

				UsernamePasswordAuthenticationToken authToken =
						new UsernamePasswordAuthenticationToken(
								user,
								null,
								user.getAuthorities()
						);

				authToken.setDetails(
						new WebAuthenticationDetailsSource()
								.buildDetails(request)
				);

				SecurityContextHolder
						.getContext()
						.setAuthentication(authToken);

				logger.info(
						"User authenticated successfully: {}",
						email
				);
			}

			filterChain.doFilter(request, response);

		} catch (Exception e) {

			logger.error(
					"JWT Authentication Error: {}",
					e.getMessage()
			);

			response.setStatus(
					HttpServletResponse.SC_UNAUTHORIZED
			);

			response.setContentType("application/json");

			response.getWriter().write(
					"""
                    {
                        "message":"Invalid or expired JWT token",
                        "success":false,
                        "status":401,
                        "errorCode":"INVALID_JWT",
                        "timestamp":%d
                    }
                    """.formatted(
							System.currentTimeMillis()
					)
			);
		}
	}

}

