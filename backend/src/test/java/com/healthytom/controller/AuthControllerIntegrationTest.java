package com.healthytom.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.healthytom.dto.LoginRequest;
import com.healthytom.dto.RegisterRequest;
import com.healthytom.entity.User;
import com.healthytom.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("dev")
@DisplayName("AuthController Integration Tests")
class AuthControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    private RegisterRequest registerRequest;
    private LoginRequest loginRequest;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();

        registerRequest = RegisterRequest.builder()
                .email("test@example.com")
                .password("Password@123")
                .firstName("John")
                .lastName("Doe")
                .phoneNumber("1234567890")
                .role("OWNER")
                .build();

        loginRequest = LoginRequest.builder()
                .email("test@example.com")
                .password("Password@123")
                .build();
    }

    @Test
    @DisplayName("Should register a new user successfully")
    void testRegisterSuccess() throws Exception {
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.accessToken").exists())
                .andExpect(jsonPath("$.refreshToken").exists())
                .andExpect(jsonPath("$.user.email").value("test@example.com"))
                .andExpect(jsonPath("$.user.firstName").value("John"));
    }

    @Test
    @DisplayName("Should fail to register with duplicate email")
    @Transactional
    void testRegisterDuplicateEmail() throws Exception {
        // First registration
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isCreated());

        // Second registration with same email
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().is5xxServerError());
    }

    @Test
    @DisplayName("Should login successfully with valid credentials")
    @Transactional
    void testLoginSuccess() throws Exception {
        // Register first
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isCreated());

        // Login
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").exists())
                .andExpect(jsonPath("$.refreshToken").exists())
                .andExpect(jsonPath("$.user.email").value("test@example.com"));
    }

    @Test
    @DisplayName("Should fail login with invalid credentials")
    void testLoginWithInvalidPassword() throws Exception {
        LoginRequest invalidRequest = LoginRequest.builder()
                .email("test@example.com")
                .password("WrongPassword")
                .build();

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Should fail login with non-existent user")
    void testLoginWithNonExistentUser() throws Exception {
        LoginRequest invalidRequest = LoginRequest.builder()
                .email("nonexistent@example.com")
                .password("Password@123")
                .build();

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Should refresh token successfully")
    @Transactional
    void testRefreshTokenSuccess() throws Exception {
        // Register and login to get tokens
        MvcResult result = mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isCreated())
                .andReturn();

        String response = result.getResponse().getContentAsString();
        String refreshToken = objectMapper.readTree(response).get("refreshToken").asText();

        // Refresh token
        String refreshRequest = "{\"refreshToken\": \"" + refreshToken + "\"}";
        mockMvc.perform(post("/api/auth/refresh-token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(refreshRequest))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").exists())
                .andExpect(jsonPath("$.refreshToken").exists());
    }

    @Test
    @DisplayName("Should register veterinarian with specialization and license")
    void testRegisterVeterinarian() throws Exception {
        RegisterRequest vetRequest = RegisterRequest.builder()
                .email("vet@example.com")
                .password("Password@123")
                .firstName("Dr.")
                .lastName("Smith")
                .phoneNumber("0987654321")
                .role("VETERINARIAN")
                .specialization("Small Animals")
                .licenseNumber("VET123456")
                .build();

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(vetRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.user.role").value("VETERINARIAN"))
                .andExpect(jsonPath("$.user.specialization").value("Small Animals"));
    }
}
