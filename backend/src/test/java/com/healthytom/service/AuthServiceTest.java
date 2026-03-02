package com.healthytom.service;

import com.healthytom.dto.*;
import com.healthytom.entity.User;
import com.healthytom.repository.UserRepository;
import com.healthytom.security.JwtTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthService Unit Tests")
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @InjectMocks
    private AuthService authService;

    private RegisterRequest registerRequest;
    private LoginRequest loginRequest;
    private User testUser;

    @BeforeEach
    void setUp() {
        registerRequest = RegisterRequest.builder()
                .email("test@example.com")
                .password("password123")
                .firstName("John")
                .lastName("Doe")
                .phoneNumber("1234567890")
                .role("OWNER")
                .build();

        loginRequest = LoginRequest.builder()
                .email("test@example.com")
                .password("password123")
                .build();

        testUser = User.builder()
                .id(1L)
                .email("test@example.com")
                .password("encodedPassword")
                .firstName("John")
                .lastName("Doe")
                .phoneNumber("1234567890")
                .role(User.UserRole.OWNER)
                .enabled(true)
                .emailVerified(false)
                .build();
    }

    @Test
    @DisplayName("Should register a new user successfully")
    void testRegisterSuccess() {
        // Arrange
        when(userRepository.existsByEmail(registerRequest.getEmail())).thenReturn(false);
        when(passwordEncoder.encode(registerRequest.getPassword())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        when(authenticationManager.authenticate(any())).thenReturn(mock(Authentication.class));
        when(jwtTokenProvider.generateAccessToken(any())).thenReturn("accessToken");
        when(jwtTokenProvider.generateRefreshToken(anyString())).thenReturn("refreshToken");
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));

        // Act
        AuthenticationResponse response = authService.register(registerRequest);

        // Assert
        assertNotNull(response);
        assertNotNull(response.getAccessToken());
        assertNotNull(response.getRefreshToken());
        assertNotNull(response.getUser());
        assertEquals("test@example.com", response.getUser().getEmail());
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    @DisplayName("Should throw exception when email already exists")
    void testRegisterWithExistingEmail() {
        // Arrange
        when(userRepository.existsByEmail(registerRequest.getEmail())).thenReturn(true);

        // Act & Assert
        assertThrows(RuntimeException.class, () -> authService.register(registerRequest));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Should login successfully with valid credentials")
    void testLoginSuccess() {
        // Arrange
        Authentication authentication = mock(Authentication.class);
        when(authenticationManager.authenticate(any())).thenReturn(authentication);
        when(jwtTokenProvider.generateAccessToken(authentication)).thenReturn("accessToken");
        when(jwtTokenProvider.generateRefreshToken("test@example.com")).thenReturn("refreshToken");
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));

        // Act
        AuthenticationResponse response = authService.login(loginRequest);

        // Assert
        assertNotNull(response);
        assertNotNull(response.getAccessToken());
        assertNotNull(response.getRefreshToken());
        verify(authenticationManager, times(1)).authenticate(any());
    }

    @Test
    @DisplayName("Should refresh token successfully")
    void testRefreshTokenSuccess() {
        // Arrange
        RefreshTokenRequest refreshRequest = RefreshTokenRequest.builder()
                .refreshToken("refreshToken")
                .build();

        when(jwtTokenProvider.validateToken("refreshToken")).thenReturn(true);
        when(jwtTokenProvider.getUsernameFromToken("refreshToken")).thenReturn("test@example.com");
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(jwtTokenProvider.generateAccessTokenFromUsername("test@example.com")).thenReturn("newAccessToken");
        when(jwtTokenProvider.generateRefreshToken("test@example.com")).thenReturn("newRefreshToken");

        // Act
        AuthenticationResponse response = authService.refreshToken(refreshRequest);

        // Assert
        assertNotNull(response);
        assertEquals("newAccessToken", response.getAccessToken());
        assertEquals("newRefreshToken", response.getRefreshToken());
    }

    @Test
    @DisplayName("Should throw exception for invalid refresh token")
    void testRefreshTokenWithInvalidToken() {
        // Arrange
        RefreshTokenRequest refreshRequest = RefreshTokenRequest.builder()
                .refreshToken("invalidToken")
                .build();

        when(jwtTokenProvider.validateToken("invalidToken")).thenReturn(false);

        // Act & Assert
        assertThrows(RuntimeException.class, () -> authService.refreshToken(refreshRequest));
    }
}
