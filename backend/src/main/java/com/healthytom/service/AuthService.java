package com.healthytom.service;

import com.healthytom.dto.*;
import com.healthytom.entity.User;
import com.healthytom.exception.EmailAlreadyExistsException;
import com.healthytom.exception.InvalidTokenException;
import com.healthytom.exception.UserNotFoundException;
import com.healthytom.repository.UserRepository;
import com.healthytom.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;

    @Transactional
    public AuthenticationResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new EmailAlreadyExistsException("Email already in use");
        }

        // Validate role - only allow OWNER and VETERINARIAN (ADMIN is internal)
        User.UserRole role;
        try {
            role = User.UserRole.valueOf(request.getRole().toUpperCase());
            if (role == User.UserRole.ADMIN) {
                throw new IllegalArgumentException("Cannot register with ADMIN role");
            }
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid role. Allowed values: OWNER, VETERINARIAN");
        }

        User user = User.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .phoneNumber(request.getPhoneNumber())
                .role(role)
                .specialization(request.getSpecialization())
                .licenseNumber(request.getLicenseNumber())
                .enabled(true)
                .emailVerified(false)
                .build();

        User savedUser = userRepository.save(user);
        log.info("New user registered with email: {}", savedUser.getEmail());

        return authenticateAndGenerateTokens(savedUser.getEmail(), request.getPassword());
    }

    @Transactional(readOnly = true)
    public AuthenticationResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );

        log.info("User logged in: {}", request.getEmail());
        return authenticateAndGenerateTokens(request.getEmail(), request.getPassword());
    }

    @Transactional
    public AuthenticationResponse refreshToken(RefreshTokenRequest request) {
        if (!jwtTokenProvider.validateToken(request.getRefreshToken())) {
            throw new InvalidTokenException("Invalid refresh token");
        }

        String email = jwtTokenProvider.getUsernameFromToken(request.getRefreshToken());
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        // Validate token version for rotation security
        Integer tokenVersion = jwtTokenProvider.getTokenVersionFromToken(request.getRefreshToken());
        Integer userTokenVersion = user.getRefreshTokenVersion();
        
        // Handle null refreshTokenVersion for existing users (defaults to 0)
        if (userTokenVersion == null) {
            userTokenVersion = 0;
            user.setRefreshTokenVersion(0);
        }
        
        if (tokenVersion == null || !tokenVersion.equals(userTokenVersion)) {
            throw new InvalidTokenException("Invalid or expired refresh token");
        }

        // Increment token version to invalidate old refresh token (token rotation)
        user.setRefreshTokenVersion(user.getRefreshTokenVersion() + 1);
        userRepository.save(user);

        String accessToken = jwtTokenProvider.generateAccessTokenFromUsername(email);
        String refreshToken = jwtTokenProvider.generateRefreshToken(email, user.getRefreshTokenVersion());

        return AuthenticationResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .user(convertToDto(user))
                .build();
    }

    private AuthenticationResponse authenticateAndGenerateTokens(String email, String password) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(email, password)
        );

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        // Initialize refreshTokenVersion if null (for existing users)
        if (user.getRefreshTokenVersion() == null) {
            user.setRefreshTokenVersion(0);
            userRepository.save(user);
        }

        String accessToken = jwtTokenProvider.generateAccessToken(authentication);
        String refreshToken = jwtTokenProvider.generateRefreshToken(email, user.getRefreshTokenVersion());

        return AuthenticationResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .user(convertToDto(user))
                .build();
    }

    private UserDto convertToDto(User user) {
        return UserDto.builder()
                .id(user.getId())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .phoneNumber(user.getPhoneNumber())
                .profileImageUrl(user.getProfileImageUrl())
                .role(user.getRole().name())
                .emailVerified(user.getEmailVerified())
                .specialization(user.getSpecialization())
                .licenseNumber(user.getLicenseNumber())
                .build();
    }
}
