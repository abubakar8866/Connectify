package com.abubakar.connectify.security;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.UUID;

import com.abubakar.connectify.dto.response.AuthResponse;
import com.abubakar.connectify.dto.response.UserResponse;
import com.abubakar.connectify.exception.OperationFailException;
import com.abubakar.connectify.service.RefreshTokenService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
	private RefreshTokenService refreshTokenService;

	@Autowired
	private JwtUtils jwtUtils;

	@Autowired
	private FileService fileService;

	@Autowired
	private ModelMapper modelMapper;

	private static final Logger logger = LoggerFactory.getLogger(OAuth2SuccessHandler.class);

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

					logger.warn("Github ID not found");

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
							"github_" + providerId + "@oauth.connectify";
					logger.warn(
							"Email is not found from github, so manually created Email : {}",email
					);

				}
			}

            default -> {
                logger.error("Unsupported provider, only Google and Github is allowed.");

                throw new UnsupportedProviderException(
                        "Unsupported provider"
                );
            }
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

			logger.info(
					"New OAuth user created | userId: {} | provider: {}",
					user.getId(),
					provider
			);
		}

		// ================= UPDATE EXISTING USER =================

		user.setName(
				name != null
						? name
						: user.getName()
		);

		if (
				user.getProvider() != AuthProvider.LOCAL
						&&
						user.getProvider() != provider
		) {

			logger.warn(
					"OAuth provider collision detected | email: {} | existingProvider: {} | attemptedProvider: {}",
					email,
					user.getProvider(),
					provider
			);

			throw new OperationFailException(
					"Account already linked with "
							+ user.getProvider()
			);
		}

		// Keep LOCAL provider unchanged
		if (user.getProvider() != AuthProvider.LOCAL) {

			user.setProvider(provider);
		}

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

		logger.info(
				"OAuth login successful | userId: {} | provider: {}",
				user.getId(),
				provider
		);

		// ================= GENERATE JWT =================

		String accessToken =
				jwtUtils.generateToken(user);

		String refreshToken =
				refreshTokenService
						.createRefreshToken(user)
						.getToken();

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
						accessToken,
						refreshToken,
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

