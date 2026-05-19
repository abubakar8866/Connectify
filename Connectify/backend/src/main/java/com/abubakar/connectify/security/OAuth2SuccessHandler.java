package com.abubakar.connectify.security;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.UUID;

import com.abubakar.connectify.dto.response.AuthResponse;
import com.abubakar.connectify.dto.response.UserResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import com.abubakar.connectify.enums.AccountStatus;
import com.abubakar.connectify.enums.AuthProvider;
import com.abubakar.connectify.enums.Role;
import com.abubakar.connectify.entity.User;
import com.abubakar.connectify.exception.GithubIdNotFound;
import com.abubakar.connectify.exception.UnsupportedProviderException;
import com.abubakar.connectify.repository.UserRepository;
import com.abubakar.connectify.service.FileService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class OAuth2SuccessHandler
		implements AuthenticationSuccessHandler {

	@Autowired
	private UserRepository userRepo;

	@Autowired
	private JwtUtils jwtUtils;

	@Autowired
	private FileService fileService;

	@Autowired
	private ModelMapper modelMapper;

	@Override
	public void onAuthenticationSuccess(
			HttpServletRequest request,
			HttpServletResponse response,
			Authentication authentication
	) throws IOException {

		OAuth2User oAuth2User =
				(OAuth2User) authentication.getPrincipal();

		String registrationId =
				((OAuth2AuthenticationToken) authentication)
						.getAuthorizedClientRegistrationId();

		String email = null;
		String name = null;
		String picture = null;
		String providerId = null;

		AuthProvider provider;

		switch (registrationId) {

			case "google" -> {

				email =
						oAuth2User.getAttribute("email");

				name =
						oAuth2User.getAttribute("name");

				picture =
						oAuth2User.getAttribute("picture");

				providerId =
						oAuth2User.getAttribute("sub");

				provider =
						AuthProvider.GOOGLE;
			}

			case "github" -> {

				email =
						(String) oAuth2User.getAttribute("email");

				name =
						(String) oAuth2User.getAttribute("name");

				Object picObj =
						oAuth2User.getAttribute("avatar_url");

				picture =
						picObj != null
								? picObj.toString()
								: null;

				Object idObj =
						oAuth2User.getAttribute("id");

				if (idObj == null) {

					throw new GithubIdNotFound(
							"Github ID not found"
					);
				}

				providerId =
						idObj.toString();

				provider =
						AuthProvider.GITHUB;

				// fallback email
				if (email == null) {

					email =
							providerId + "@github.com";
				}
			}

			default ->
					throw new UnsupportedProviderException(
							"Unsupported provider"
					);
		}

		User user =
				userRepo.findByEmail(email)
						.orElse(null);

		// ================= CREATE USER =================

		if (user == null) {

			user = new User();

			user.setEmail(email);

			user.setName(name);

			// AUTO USERNAME
			String username;

			if (
					email != null
							&& email.contains("@")
			) {

				username =
						email.substring(
								0,
								email.indexOf("@")
						);

			} else {

				username =
						"user_" + providerId;
			}

			// AVOID DUPLICATE USERNAME
			username =
					username + "_"
							+ UUID.randomUUID()
							.toString()
							.substring(0, 5);

			user.setUname(username);

			user.setProvider(provider);

			user.setProviderId(providerId);

			user.setRole(Role.USER);

			user.setPassword("");

			user.setAccountStatus(
					AccountStatus.ACTIVE
			);

			user.setIsActive(true);

			user.setIsEmailVerified(true);

			user.setLastLoginAt(
					LocalDateTime.now()
			);

			user =
					userRepo.save(user);
		}

		// ================= UPDATE EXISTING USER =================

		user.setName(
				name != null
						? name
						: user.getName()
		);

		user.setProvider(provider);

		user.setProviderId(providerId);

		user.setLastLoginAt(
				LocalDateTime.now()
		);

		// ================= PROFILE IMAGE =================

		if (
				picture != null
						&&
						(
								user.getProfileImageUrl() == null
										|| user.getProfileImageUrl().isBlank()
						)
		) {

			String fileName =
					fileService.uploadFromUrl(
							picture,
							user.getId(),
							user.getProfileImageUrl(),
							"users"
					);

			if (fileName != null) {

				user.setProfileImageUrl(fileName);
			}
		}

		userRepo.save(user);

		// ================= GENERATE JWT =================

		String token =
				jwtUtils.generateToken(user);

		// ================= RESPONSE =================

		UserResponse userResponse =
				modelMapper.map(
						user,
						UserResponse.class
				);

		userResponse.setAge(
				user.getAge()
		);

		AuthResponse authResponse =
				new AuthResponse(
						token,
						"Bearer",
						userResponse
				);

		response.setContentType(
				"application/json"
		);

		response.setCharacterEncoding(
				"UTF-8"
		);

		new ObjectMapper()
				.writeValue(
						response.getWriter(),
						authResponse
				);
	}

}

