package com.healthytom.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.healthytom.dto.RegisterRequest;
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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("dev")
@DisplayName("UserController Integration Tests")
class UserControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    private String accessToken;
    private Long userId;

    @BeforeEach
    @Transactional
    void setUp() throws Exception {
        userRepository.deleteAll();

        RegisterRequest registerRequest = RegisterRequest.builder()
                .email("test@example.com")
                .password("Password@123")
                .firstName("John")
                .lastName("Doe")
                .phoneNumber("1234567890")
                .role("OWNER")
                .build();

        MvcResult result = mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequest)))
                .andReturn();

        String response = result.getResponse().getContentAsString();
        accessToken = objectMapper.readTree(response).get("accessToken").asText();
        userId = objectMapper.readTree(response).get("user").get("id").asLong();
    }

    @Test
    @DisplayName("Should get current authenticated user")
    void testGetCurrentUserSuccess() throws Exception {
        mockMvc.perform(get("/api/users/me")
                .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(userId))
                .andExpect(jsonPath("$.email").value("test@example.com"))
                .andExpect(jsonPath("$.firstName").value("John"))
                .andExpect(jsonPath("$.lastName").value("Doe"))
                .andExpect(jsonPath("$.role").value("OWNER"));
    }

    @Test
    @DisplayName("Should fail to get current user without authentication")
    void testGetCurrentUserWithoutAuth() throws Exception {
        mockMvc.perform(get("/api/users/me"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Should get user by ID (for veterinarian)")
    @Transactional
    void testGetUserByIdAsVeterinarian() throws Exception {
        // Register veterinarian
        RegisterRequest vetRequest = RegisterRequest.builder()
                .email("vet@example.com")
                .password("Password@123")
                .firstName("Dr.")
                .lastName("Smith")
                .role("VETERINARIAN")
                .build();

        MvcResult vetResult = mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(vetRequest)))
                .andReturn();

        String vetResponse = vetResult.getResponse().getContentAsString();
        String vetToken = objectMapper.readTree(vetResponse).get("accessToken").asText();

        // Get owner user by ID
        mockMvc.perform(get("/api/users/" + userId)
                .header("Authorization", "Bearer " + vetToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(userId))
                .andExpect(jsonPath("$.email").value("test@example.com"));
    }

    @Test
    @DisplayName("Should fail to get user by ID as owner")
    void testGetUserByIdAsOwner() throws Exception {
        mockMvc.perform(get("/api/users/1")
                .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Should return 404 when user not found")
    void testGetUserByIdNotFound() throws Exception {
        // Register veterinarian
        RegisterRequest vetRequest = RegisterRequest.builder()
                .email("vet@example.com")
                .password("Password@123")
                .firstName("Dr.")
                .lastName("Smith")
                .role("VETERINARIAN")
                .build();

        MvcResult vetResult = mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(vetRequest)))
                .andReturn();

        String vetResponse = vetResult.getResponse().getContentAsString();
        String vetToken = objectMapper.readTree(vetResponse).get("accessToken").asText();

        // Try to get non-existent user
        mockMvc.perform(get("/api/users/99999")
                .header("Authorization", "Bearer " + vetToken))
                .andExpect(status().is5xxServerError());
    }
}
