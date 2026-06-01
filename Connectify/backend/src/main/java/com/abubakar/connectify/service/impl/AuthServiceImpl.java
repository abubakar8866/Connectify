package com.abubakar.connectify.service.impl;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Objects;
import java.util.UUID;

import com.abubakar.connectify.dto.request.*;
import com.abubakar.connectify.entity.RefreshToken;
import com.abubakar.connectify.service.RefreshTokenService;
import com.abubakar.connectify.util.AuthUtil;
import com.abubakar.connectify.util.UserAccessValidator;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.abubakar.connectify.dto.response.AuthResponse;
import com.abubakar.connectify.dto.response.UserResponse;
import com.abubakar.connectify.entity.User;
import com.abubakar.connectify.enums.AccountStatus;
import com.abubakar.connectify.enums.AuthProvider;
import com.abubakar.connectify.enums.Role;
import com.abubakar.connectify.exception.EmailNotFound;
import com.abubakar.connectify.exception.OperationFailException;
import com.abubakar.connectify.exception.ResourceAlreadyExistsException;
import com.abubakar.connectify.repository.UserRepository;
import com.abubakar.connectify.security.JwtUtils;
import com.abubakar.connectify.service.AuthService;
import com.abubakar.connectify.service.FileService;

@Service
public class AuthServiceImpl implements AuthService {

	@Autowired
	private ModelMapper modelMapper;

	@Autowired
	private UserRepository userRepo;

	@Autowired
	private RefreshTokenService refreshTokenService;

	@Autowired
	private PasswordEncoder passwordEncoder;

	@Autowired
	private AuthenticationManager authenticationManager;

	@Autowired
	private JwtUtils jwtUtil;

	@Autowired
	private AuthUtil authUtil;

	@Autowired
	private FileService fileService;

	@Autowired
	private JavaMailSender mailSender;

	@Autowired
	private UserAccessValidator userAccessValidator;

	@Value("${app.frontend.url}")
	private String frontendUrl;

	private static final Logger logger = LoggerFactory.getLogger(AuthServiceImpl.class);

	// ================= REGISTER =================
	@Override
	@Transactional
	public AuthResponse register(RegisterRequest request) {

		logger.debug(
				"Starting register | email: {} | username: {}",
				request.getEmail(),
				request.getUname()
		);

		if (userRepo.existsByEmail(request.getEmail())) {

			logger.warn(
					"Registration failed - email already exists | email: {}",
					request.getEmail()
			);

			throw new ResourceAlreadyExistsException(
					"Email already exists"
			);
		}

		if (userRepo.existsByUname(request.getUname())) {

			logger.warn(
					"Registration failed - username already exists | username: {}",
					request.getUname()
			);

			throw new ResourceAlreadyExistsException(
					"Username already exists"
			);
		}

		User user = new User();

		user.setName(request.getName());
		user.setUname(request.getUname());
		user.setEmail(request.getEmail().trim().toLowerCase());

		user.setPassword(
				passwordEncoder.encode(request.getPassword())
		);

		user.setRole(Role.USER);

		user.setProvider(AuthProvider.LOCAL);

		user.setAccountStatus(AccountStatus.ACTIVE);

		user.setIsActive(true);

		try {

			userRepo.save(user);

			logger.info(
					"User registered successfully | userId: {} | email: {}",
					user.getId(),
					user.getEmail()
			);

		} catch (DataIntegrityViolationException e) {

			logger.error(
					"Registration failed | email: {}",
					request.getEmail(),
					e
			);

			throw new ResourceAlreadyExistsException(
					"User already exists"
			);
		}

		return new AuthResponse(
				null,
				null,
				"Bearer",
				this.mapToResponse(user)
		);

	}

	// ================= LOGIN =================
	@Override
	@Transactional
	public AuthResponse login(LoginRequest request) {

		logger.debug(
				"Starting login | email: {}",
				request.getEmail()
		);

		try {

			Authentication authentication =
					authenticationManager.authenticate(
							new UsernamePasswordAuthenticationToken(
									request.getEmail().trim().toLowerCase(),
									request.getPassword()
							)
					);

			User user = (User) authentication.getPrincipal();

            assert user != null;
            userAccessValidator.validateActiveUser(user);

			Objects.requireNonNull(user).setLastLoginAt(LocalDateTime.now());

			userRepo.save(user);

			String accessToken = jwtUtil.generateToken(user);

			String refreshToken =
					refreshTokenService
							.createRefreshToken(user)
							.getToken();

			logger.info(
					"Login successful | userId: {} | email: {}",
					user.getId(),
					user.getEmail()
			);

			return new AuthResponse(
					accessToken,
					refreshToken,
					"Bearer",
					this.mapToResponse(user)
			);

		} catch (BadCredentialsException e) {

			logger.error(
					"Login failed unexpectedly | email: {}",
					request.getEmail(),
					e
			);

			throw new OperationFailException(
					"Invalid email or password"
			);
		}
	}

	// ================= REFRESH TOKEN =================
	@Override
	@Transactional
	public AuthResponse refreshToken(
			String refreshToken
	) {

		logger.debug(
				"Refresh token request received"
		);

		RefreshToken token =
				refreshTokenService
						.verifyRefreshToken(
								refreshToken
						);

		User user =
				token.getUser();

		userAccessValidator.validateActiveUser(
				user
		);

		String accessToken =
				jwtUtil.generateToken(user);

		// ROTATE REFRESH TOKEN
		refreshTokenService.deleteByUser(
				user
		);

		RefreshToken newRefreshToken =
				refreshTokenService.createRefreshToken(
						user
				);

		logger.info(
				"Access token refreshed | userId: {}",
				user.getId()
		);

		return new AuthResponse(
				accessToken,
				newRefreshToken.getToken(),
				"Bearer",
				mapToResponse(user)
		);
	}

	// ================= LOGOUT =================
	@Override
	@Transactional
	public void logout() {

		User currentUser =
				authUtil.getCurrentUser();

		refreshTokenService.deleteByUser(
				currentUser
		);

		logger.info(
				"User logged out | userId: {}",
				currentUser.getId()
		);

	}

	// ================= GET CURRENT USER =================
	@Override
	public UserResponse getCurrentUser() {

		logger.debug(
				"Fetching current user"
		);

		User user = this.authUtil.getCurrentUser();

		logger.info(
				"Current user fetched successfully | userId: {}",
				user.getId()
		);

		return mapToResponse(user);
	}

	// ================= UPDATE PROFILE =================
	@Override
	@Transactional
	public UserResponse updateProfile(
			Long userId,
			UpdateProfileRequest request,
			MultipartFile file
	) {

		logger.debug(
				"Starting profile update | userId: {}",
				userId
		);

		User user =
				userAccessValidator.getValidUser(userId);

		User currentUser =
				this.authUtil.getCurrentUser();

		// Authorization
		if (!user.getId().equals(currentUser.getId())) {

			logger.warn(
					"Unauthorized profile update attempt | currentUserId: {} | targetUserId: {}",
					currentUser.getId(),
					userId
			);

			throw new OperationFailException(
					"Unauthorized access"
			);
		}

		// Username validation
		if (
				request.getUname() != null
						&&
						!request.getUname().equals(user.getUname())
		) {

			if (
					userRepo.existsByUname(
							request.getUname()
					)
			) {

				logger.warn(
						"Profile update failed - username already exists | username: {}",
						request.getUname()
				);

				throw new ResourceAlreadyExistsException(
						"Username already exists"
				);
			}

			user.setUname(
					request.getUname()
			);
		}

		// Name
		if (request.getName() != null) {
			user.setName(request.getName());
		}

		// Bio
		if (request.getBio() != null) {
			user.setBio(request.getBio());
		}

		// Gender
		if (request.getGender() != null) {
			user.setGender(request.getGender());
		}

		// Languages
		if (request.getLanguages() != null) {
			user.setLanguages(request.getLanguages());
		}

		// Date of birth
		if (request.getDateOfBirth() != null) {
			user.setDateOfBirth(
					request.getDateOfBirth()
			);
		}

		// City
		if (request.getCity() != null) {
			user.setCity(request.getCity());
		}

		String uploadedFileName = null;

		// STORE OLD FILE
		String oldProfileImage =
				user.getProfileImageUrl();

		try {

			// ================= PROFILE IMAGE =================

			if (
					file != null
							&&
							!file.isEmpty()
			) {

				uploadedFileName =
						fileService.uploadFile(
								file,
								userId,
								null, // IMPORTANT
								"users"
						);

				user.setProfileImageUrl(
						uploadedFileName
				);
			}

			// ================= SAVE USER =================

			userRepo.save(user);

			// ================= DELETE OLD FILE =================

			if (
					uploadedFileName != null
							&&
							oldProfileImage != null
							&&
							!oldProfileImage.isBlank()
			) {

				fileService.deleteFile(
						oldProfileImage,
						"users"
				);
			}

			logger.info(
					"Profile updated successfully | userId: {}",
					userId
			);

			return mapToResponse(user);

		} catch (Exception e) {

			logger.error(
					"Profile update failed | userId: {}",
					userId,
					e
			);

			// DELETE NEWLY UPLOADED FILE
			if (uploadedFileName != null) {

				fileService.deleteFile(
						uploadedFileName,
						"users"
				);
			}

			throw e;
		}
	}

	// ================= FORGOT PASSWORD =================
	@Override
	@Transactional
	public void forgotPassword(ForgotPasswordRequest request) {

		logger.debug(
				"Starting forgot password flow"
		);

		String email = request.getEmail();

		User user =
				userRepo.findByEmail(
						email.trim().toLowerCase()
				).orElse(null);

		// SECURITY:
		// Never reveal whether email exists

		if (user == null) {

			logger.warn(
					"Forgot password requested for non-existing email"
			);

			throw new OperationFailException("Email is not registered.");
		}

		userAccessValidator.validateActiveUser(user);

		String token =
				UUID.randomUUID().toString();

		logger.debug(
				"Password reset token generated | userId: {}",
				user.getId()
		);

		user.setResetToken(token);

		user.setResetTokenExpiry(
				LocalDateTime.now().plusMinutes(15)
		);

		userRepo.save(user);

		String resetLink =
				frontendUrl +
						"/reset-password?token=" +
						token;

		sendResetEmail(
				user.getEmail().trim().toLowerCase(),
				resetLink
		);

		logger.info(
				"Password reset email sent successfully | userId: {}",
				user.getId()
		);
	}

	// ================= RESET PASSWORD =================
	@Override
	@Transactional
	public void resetPassword(
			String token,
			ResetPasswordRequest request
	) {

		logger.debug(
				"Starting password reset"
		);

		if (token == null || token.trim().isEmpty()) {
			logger.warn(
					"Password reset failed - invalid token"
			);
			throw new OperationFailException("Invalid reset token");
		}

		String cleanToken = token.trim();

		User user = userRepo.findByResetToken(cleanToken)
				.orElseThrow(() -> {
						logger.warn(
								"Password reset failed - invalid token"
						);
						return new OperationFailException(
									"Invalid reset token"
						);
					}
				);

		if (
				user.getResetTokenExpiry()
						.isBefore(LocalDateTime.now())
		) {

			logger.warn(
					"Password reset failed - token expired | userId: {}",
					user.getId()
			);

			throw new OperationFailException(
					"Reset token expired"
			);
		}

		if (!cleanToken.equals(user.getResetToken())) {

			logger.warn(
					"Password reset failed - token expired | userId: {}",
					user.getId()
			);

			throw new OperationFailException("Invalid reset token");

		}

		User validateUser = userAccessValidator.getValidUser(user.getId());

		validateUser.setPassword(
				passwordEncoder.encode(request.getPassword())
		);

		validateUser.setResetToken(null);

		validateUser.setResetTokenExpiry(null);

		userRepo.save(validateUser);

		logger.info(
				"Password reset successful | userId: {} | token: {}",
				validateUser.getId(),cleanToken
		);
	}

	// ================= CREATING ADMIN =================
	@Override
	@Transactional
	public AuthResponse createAdmin(RegisterRequest request) {

		logger.debug(
				"Starting admin creation | email: {}",
				request.getEmail()
		);

		// Check if admin already exists
		if (userRepo.existsByRole(Role.ADMIN)) {

			logger.warn(
					"Admin creation blocked - admin already exists"
			);

			throw new OperationFailException(
					"Admin already created. Access denied."
			);
		}

		// Email check
		if (userRepo.existsByEmail(request.getEmail())) {

			logger.warn(
					"Admin creation failed - email already exists | email: {}",
					request.getEmail()
			);

			throw new ResourceAlreadyExistsException(
					"Email already exists"
			);
		}

		// Username check
		if (userRepo.existsByUname(request.getUname())) {

			logger.warn(
					"Admin creation failed - username already exists | username: {}",
					request.getUname()
			);

			throw new ResourceAlreadyExistsException(
					"Username already exists"
			);
		}

		User admin = new User();

		admin.setName(request.getName());

		admin.setUname(request.getUname());

		admin.setEmail(request.getEmail().trim());

		admin.setPassword(
				passwordEncoder.encode(request.getPassword())
		);

		admin.setRole(Role.ADMIN);

		admin.setProvider(AuthProvider.LOCAL);

		admin.setAccountStatus(AccountStatus.ACTIVE);

		admin.setIsActive(true);

		userRepo.save(admin);

		logger.info(
				"Admin created successfully | adminId: {}",
				admin.getId()
		);

		String accessToken =
				jwtUtil.generateToken(admin);

		String refreshToken =
				refreshTokenService
						.createRefreshToken(admin)
						.getToken();

		return new AuthResponse(
				accessToken,
				refreshToken,
				"Bearer",
				mapToResponse(admin)
		);

	}

	// ================= SEND EMAIL VERIFICATION =================
	@Override
	@Transactional
	public void sendEmailVerification() {

		User currentUser = this.authUtil.getCurrentUser();

		logger.debug(
				"Starting email verification process | userId: {}",
				currentUser.getId()
		);

		if (Boolean.TRUE.equals(
				currentUser.getIsEmailVerified()
		)) {

			logger.warn(
					"Email verification skipped - already verified | userId: {}",
					currentUser.getId()
			);

			throw new OperationFailException(
					"Email already verified"
			);
		}

		String token =
				UUID.randomUUID().toString();

		currentUser.setEmailVerificationToken(token);

		currentUser.setEmailVerificationExpiry(
				LocalDateTime.now().plusMinutes(30)
		);

		userRepo.save(currentUser);

		String verificationUrl =
				frontendUrl +
						"/verify-email?token="
						+ token;

		try {

			SimpleMailMessage message =
					new SimpleMailMessage();

			message.setTo(currentUser.getEmail());

			message.setSubject(
					"Verify Your Connectify Account"
			);

			message.setText(
					"Click below link to verify email:\n\n"
							+ verificationUrl
							+ "\n\nLink expires in 30 minutes."
			);

			mailSender.send(message);

		} catch (Exception e) {

			throw new OperationFailException(
					"Failed to send verification email"
			);
		}

		logger.info(
				"Verification email sent successfully | userId: {}",
				currentUser.getId()
		);
	}

	// ================= VERIFY EMAIL =================
	@Override
	@Transactional
	public void verifyEmail(String token) {

		logger.debug(
				"Starting email verification"
		);

		User user =
				userRepo.findByEmailVerificationToken(token)
						.orElseThrow(() -> {
								logger.warn(
										"Email verification failed - invalid token"
								);
								return new OperationFailException(
										"Invalid verification token"
								);
							}
						);

		if (
				user.getEmailVerificationExpiry()
						.isBefore(LocalDateTime.now())
		) {

			logger.warn(
					"Email verification failed - token expired | userId: {}",
					user.getId()
			);

			throw new OperationFailException(
					"Verification token expired"
			);
		}

		User validateUser = userAccessValidator.getValidUser(user.getId());

		validateUser.setIsEmailVerified(true);

		validateUser.setEmailVerificationToken(null);

		validateUser.setEmailVerificationExpiry(null);

		userRepo.save(validateUser);

		logger.info(
				"Email verified successfully | userId: {}",
				validateUser.getId()
		);
	}

	// ================= DEACTIVATE ACCOUNT =================
	@Override
	@Transactional
	public void deactivateMyAccount() {

		User currentUser = this.authUtil.getCurrentUser();

		logger.debug(
				"Starting account deactivation | userId: {}",
				currentUser.getId()
		);

		currentUser.setDeleted(true);

		currentUser.setIsActive(false);

		currentUser.setDeletedAt(LocalDateTime.now());

		currentUser.setAccountStatus(
				AccountStatus.DEACTIVATED
		);

		userRepo.save(currentUser);

		refreshTokenService.deleteByUser(currentUser);

		logger.info(
				"Account deactivated successfully | userId: {}",
				currentUser.getId()
		);

	}

	// ================= REQUEST ACCOUNT RESTORE =================
	@Override
	@Transactional
	public void requestAccountRestore() {

		User currentUser = this.authUtil.getCurrentUser();

		logger.debug(
				"Starting account restore request | userId: {}",
				currentUser.getId()
		);

		if (!Boolean.TRUE.equals(currentUser.getDeleted())) {

			logger.warn(
					"Restore request failed - account not deactivated | userId: {}",
					currentUser.getId()
			);

			throw new OperationFailException(
					"Account is not deactivated"
			);
		}

		currentUser.setRestoreRequested(true);

		userRepo.save(currentUser);

		logger.info(
				"Restore request submitted successfully | userId: {}",
				currentUser.getId()
		);

	}

	// ================= REQUEST UNBAN APPEAL =================
	@Override
	@Transactional
	public void requestUnbanAppeal(
			String message
	) {

		User currentUser = this.authUtil.getCurrentUser();

		logger.debug(
				"Starting unban appeal request | userId: {}",
				currentUser.getId()
		);

		if (
				currentUser.getAccountStatus()
						!= AccountStatus.BANNED
		) {

			logger.warn(
					"Unban appeal failed - user not banned | userId: {}",
					currentUser.getId()
			);

			throw new OperationFailException(
					"User is not banned"
			);
		}

		currentUser.setUnbanRequested(true);

		currentUser.setUnbanAppealMessage(message);

		userRepo.save(currentUser);

		logger.info(
				"Unban appeal submitted successfully | userId: {}",
				currentUser.getId()
		);

	}

	// ================= SEND RESET EMAIL =================
	private void sendResetEmail(
			String toEmail,
			String resetUrl
	) {

		try {

			SimpleMailMessage message =
					new SimpleMailMessage();

			message.setTo(toEmail);

			message.setSubject(
					"Connectify Password Reset"
			);

			message.setText(
					"Click the link below to reset your password:\n\n"
							+ resetUrl
							+ "\n\nThis link expires in 15 minutes."
			);

			mailSender.send(message);

			logger.info(
					"Reset email sent to: {}",
					toEmail
			);

		} catch (Exception e) {

			logger.error(
					"Failed to send reset email",
					e
			);

			throw new OperationFailException(
					"Failed to send reset email"
			);
		}
	}

	// ================= ENTITY TO RESPONSE =================
	private UserResponse mapToResponse(User user) {

		UserResponse response = new UserResponse();

		response.setId(user.getId());
		response.setName(user.getName());
		response.setUname(user.getUname());
		response.setEmail(user.getEmail());

		response.setBio(user.getBio());
		response.setProfileImageUrl(user.getProfileImageUrl());

		response.setRole(user.getRole());
		response.setGender(user.getGender());

		// IMPORTANT FIX (avoid Hibernate lazy issue)
		response.setLanguages(
				user.getLanguages() != null
						? new ArrayList<>(user.getLanguages())
						: new ArrayList<>()
		);

		response.setDateOfBirth(user.getDateOfBirth());
		response.setAge(user.getAge());

		response.setCity(user.getCity());

		response.setAccountStatus(user.getAccountStatus());
		response.setProvider(user.getProvider());

		response.setIsActive(user.getIsActive());
		response.setIsEmailVerified(user.getIsEmailVerified());

		response.setCreatedAt(user.getCreatedAt());
		response.setUpdatedAt(user.getUpdatedAt());
		response.setLastLoginAt(user.getLastLoginAt());

		return response;

	}

}

