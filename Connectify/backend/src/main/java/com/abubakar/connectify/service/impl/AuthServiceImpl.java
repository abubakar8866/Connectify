package com.abubakar.connectify.service.impl;

import java.time.LocalDateTime;
import java.util.UUID;

import com.abubakar.connectify.dto.request.UpdateProfileRequest;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.abubakar.connectify.dto.request.LoginRequest;
import com.abubakar.connectify.dto.request.RegisterRequest;
import com.abubakar.connectify.dto.request.ResetPasswordRequest;
import com.abubakar.connectify.dto.response.AuthResponse;
import com.abubakar.connectify.dto.response.UserResponse;
import com.abubakar.connectify.entity.User;
import com.abubakar.connectify.enums.AccountStatus;
import com.abubakar.connectify.enums.AuthProvider;
import com.abubakar.connectify.enums.Role;
import com.abubakar.connectify.exception.EmailNotFound;
import com.abubakar.connectify.exception.OperationFailException;
import com.abubakar.connectify.exception.ResourceAlreadyExistsException;
import com.abubakar.connectify.exception.ResourceNotFound;
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
	private PasswordEncoder passwordEncoder;

	@Autowired
	private AuthenticationManager authenticationManager;

	@Autowired
	private JwtUtils jwtUtil;

	@Autowired
	private FileService fileService;

	@Autowired
	private JavaMailSender mailSender;

	private static final Logger logger = LoggerFactory.getLogger(AuthServiceImpl.class);

	// ================= REGISTER =================
	@Override
	@Transactional
	public AuthResponse register(RegisterRequest request) {

		logger.info("Register request for email: {}", request.getEmail());

		if (userRepo.existsByEmail(request.getEmail())) {
			throw new ResourceAlreadyExistsException(
					"Email already exists"
			);
		}

		if (userRepo.existsByUname(request.getUname())) {
			throw new ResourceAlreadyExistsException(
					"Username already exists"
			);
		}

		User user = new User();

		user.setName(request.getName());
		user.setUname(request.getUname());
		user.setEmail(request.getEmail().trim());

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
					"User registered successfully: {}",
					user.getEmail()
			);

		} catch (DataIntegrityViolationException e) {

			logger.error("Registration failed", e);

			throw new ResourceAlreadyExistsException(
					"User already exists"
			);
		}

		return new AuthResponse("","none",this.mapToResponse(user));
	}

	// ================= LOGIN =================
	@Override
	public AuthResponse login(LoginRequest request) {

		logger.info("Login request for: {}", request.getEmail());

		try {

			Authentication authentication =
					authenticationManager.authenticate(
							new UsernamePasswordAuthenticationToken(
									request.getEmail().trim(),
									request.getPassword()
							)
					);

			User user = (User) authentication.getPrincipal();

			user.setLastLoginAt(LocalDateTime.now());

			userRepo.save(user);

			String token = jwtUtil.generateToken(user);

			logger.info(
					"Login successful for: {}",
					user.getEmail()
			);

			return new AuthResponse(token, "Bearer", this.mapToResponse(user));

		} catch (BadCredentialsException e) {

			logger.error(
					"Invalid credentials for: {}",
					request.getEmail()
			);

			throw new OperationFailException(
					"Invalid email or password"
			);
		}
	}

	// ================= GET CURRENT USER =================
	@Override
	public UserResponse getCurrentUser() {

		Authentication authentication =
				SecurityContextHolder.getContext()
						.getAuthentication();

		if (authentication == null || !authentication.isAuthenticated()) {

			throw new OperationFailException(
					"User not authenticated"
			);
		}

		User user = (User) authentication.getPrincipal();

		logger.info(
				"Current user fetched: {}",
				user.getEmail()
		);

		return mapToResponse(user);
	}

	// ================= UPDATE PROFILE IMAGE =================
	@Override
	@Transactional
	public UserResponse updateProfile(
			Long userId,
			UpdateProfileRequest request,
			MultipartFile file
	) {

		logger.info(
				"Updating profile for userId: {}",
				userId
		);

		User user = userRepo.findById(userId)
				.orElseThrow(() ->
						new ResourceNotFound(
								"User not found"
						)
				);

		Authentication authentication =
				SecurityContextHolder.getContext()
						.getAuthentication();

		User currentUser =
				(User) authentication.getPrincipal();

		// Authorization
		if (!user.getId().equals(currentUser.getId())) {

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

				throw new ResourceAlreadyExistsException(
						"Username already exists"
				);
			}

			user.setUname(request.getUname());
		}

		// Name
		if (request.getName() != null) {
			user.setName(request.getName());
		}

		// Bio
		if (request.getBio() != null) {
			user.setBio(request.getBio());
		}

		// Profile image
		if (file != null && !file.isEmpty()) {

			String fileName = fileService.uploadFile(
					file,
					userId,
					user.getProfileImageUrl(),
					"users"
			);

			user.setProfileImageUrl(fileName);
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
			user.setDateOfBirth(request.getDateOfBirth());
		}

		// City
		if (request.getCity() != null) {
			user.setCity(request.getCity());
		}

		userRepo.save(user);

		logger.info(
				"Profile updated successfully for userId: {}",
				userId
		);

		return mapToResponse(user);
	}


	// ================= FORGOT PASSWORD =================
	@Override
	@Transactional
	public void forgotPassword(String email) {

		logger.info(
				"Forgot password request for: {}",
				email
		);

		User user = userRepo.findByEmail(email)
				.orElseThrow(() ->
						new EmailNotFound(
								"User not found with email: " + email
						)
				);

		String token = UUID.randomUUID().toString();

		user.setResetToken(token);

		user.setResetTokenExpiry(
				LocalDateTime.now().plusMinutes(15)
		);

		userRepo.save(user);

		String resetUrl =
				"http://localhost:3000/reset-password?token="
						+ token;

		sendResetEmail(
				user.getEmail(),
				resetUrl
		);

		logger.info(
				"Password reset email sent to: {}",
				email
		);
	}

	// ================= RESET PASSWORD =================
	@Override
	@Transactional
	public void resetPassword(
			String token,
			ResetPasswordRequest request
	) {

		User user = userRepo.findByResetToken(token)
				.orElseThrow(() ->
						new OperationFailException(
								"Invalid reset token"
						)
				);

		if (
				user.getResetTokenExpiry()
						.isBefore(LocalDateTime.now())
		) {

			throw new OperationFailException(
					"Reset token expired"
			);
		}

		user.setPassword(
				passwordEncoder.encode(request.getPassword())
		);

		user.setResetToken(null);

		user.setResetTokenExpiry(null);

		userRepo.save(user);

		logger.info(
				"Password reset successful for: {}",
				user.getEmail()
		);
	}

	// ================= CREATING ADMIN =================
	@Override
	@Transactional
	public AuthResponse createAdmin(RegisterRequest request) {

		logger.info(
				"Admin creation request for: {}",
				request.getEmail()
		);

		// Check if admin already exists
		if (userRepo.existsByRole(Role.ADMIN)) {

			logger.warn("Admin already exists");

			throw new OperationFailException(
					"Admin already created. Access denied."
			);
		}

		// Email check
		if (userRepo.existsByEmail(request.getEmail())) {

			throw new ResourceAlreadyExistsException(
					"Email already exists"
			);
		}

		// Username check
		if (userRepo.existsByUname(request.getUname())) {

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
				"Admin created successfully: {}",
				admin.getEmail()
		);

		String token = jwtUtil.generateToken(admin);

		return new AuthResponse(
				token,
				"Bearer",
				mapToResponse(admin)
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

		UserResponse response =
				modelMapper.map(user, UserResponse.class);

		response.setAge(user.getAge());

		return response;

	}

}

