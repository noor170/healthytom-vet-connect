package com.healthytom.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.healthytom.dto.LoginRequest;
import com.healthytom.dto.PetDto;
import com.healthytom.dto.RegisterRequest;
import com.healthytom.entity.Pet;
import com.healthytom.entity.User;
import com.healthytom.repository.PetRepository;
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

import java.time.LocalDate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("dev")
@DisplayName("PetController Integration Tests")
class PetControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PetRepository petRepository;

    private String accessToken;
    private Long ownerId;

    @BeforeEach
    @Transactional
    void setUp() throws Exception {
        petRepository.deleteAll();
        userRepository.deleteAll();

        // Register owner
        RegisterRequest registerRequest = RegisterRequest.builder()
                .email("owner@example.com")
                .password("Password@123")
                .firstName("John")
                .lastName("Doe")
                .role("OWNER")
                .build();

        MvcResult registerResult = mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequest)))
                .andReturn();

        String registerResponse = registerResult.getResponse().getContentAsString();
        accessToken = objectMapper.readTree(registerResponse).get("accessToken").asText();
        ownerId = objectMapper.readTree(registerResponse).get("user").get("id").asLong();
    }

    @Test
    @DisplayName("Should create a pet successfully")
    void testCreatePetSuccess() throws Exception {
        PetDto petDto = PetDto.builder()
                .name("Buddy")
                .species("Dog")
                .breed("Golden Retriever")
                .color("Golden")
                .weight(30.5)
                .dateOfBirth(LocalDate.of(2020, 1, 15))
                .build();

        mockMvc.perform(post("/api/pets?ownerId=" + ownerId)
                .header("Authorization", "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(petDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.name").value("Buddy"))
                .andExpect(jsonPath("$.species").value("Dog"))
                .andExpect(jsonPath("$.breed").value("Golden Retriever"));
    }

    @Test
    @DisplayName("Should get pet by ID successfully")
    @Transactional
    void testGetPetById() throws Exception {
        // Create pet first
        PetDto petDto = PetDto.builder()
                .name("Max")
                .species("Cat")
                .breed("Siamese")
                .color("Cream")
                .weight(4.5)
                .build();

        MvcResult createResult = mockMvc.perform(post("/api/pets?ownerId=" + ownerId)
                .header("Authorization", "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(petDto)))
                .andReturn();

        Long petId = objectMapper.readTree(createResult.getResponse().getContentAsString())
                .get("id").asLong();

        // Get pet
        mockMvc.perform(get("/api/pets/" + petId)
                .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(petId))
                .andExpect(jsonPath("$.name").value("Max"))
                .andExpect(jsonPath("$.species").value("Cat"));
    }

    @Test
    @DisplayName("Should get all pets for owner")
    @Transactional
    void testGetPetsByOwnerId() throws Exception {
        // Create two pets
        PetDto pet1 = PetDto.builder()
                .name("Buddy")
                .species("Dog")
                .breed("Golden Retriever")
                .weight(30.5)
                .build();

        PetDto pet2 = PetDto.builder()
                .name("Max")
                .species("Cat")
                .breed("Siamese")
                .weight(4.5)
                .build();

        mockMvc.perform(post("/api/pets?ownerId=" + ownerId)
                .header("Authorization", "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(pet1)));

        mockMvc.perform(post("/api/pets?ownerId=" + ownerId)
                .header("Authorization", "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(pet2)));

        // Get pets by owner
        mockMvc.perform(get("/api/pets/owner/" + ownerId)
                .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].name").value("Buddy"))
                .andExpect(jsonPath("$[1].name").value("Max"));
    }

    @Test
    @DisplayName("Should update pet successfully")
    @Transactional
    void testUpdatePetSuccess() throws Exception {
        // Create pet
        PetDto petDto = PetDto.builder()
                .name("Buddy")
                .species("Dog")
                .breed("Golden Retriever")
                .weight(30.5)
                .build();

        MvcResult createResult = mockMvc.perform(post("/api/pets?ownerId=" + ownerId)
                .header("Authorization", "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(petDto)))
                .andReturn();

        Long petId = objectMapper.readTree(createResult.getResponse().getContentAsString())
                .get("id").asLong();

        // Update pet
        PetDto updateDto = PetDto.builder()
                .name("Buddy Updated")
                .species("Dog")
                .breed("Golden Retriever")
                .weight(31.0)
                .build();

        mockMvc.perform(put("/api/pets/" + petId)
                .header("Authorization", "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(petId))
                .andExpect(jsonPath("$.name").value("Buddy Updated"))
                .andExpect(jsonPath("$.weight").value(31.0));
    }

    @Test
    @DisplayName("Should delete pet successfully")
    @Transactional
    void testDeletePetSuccess() throws Exception {
        // Create pet
        PetDto petDto = PetDto.builder()
                .name("Buddy")
                .species("Dog")
                .breed("Golden Retriever")
                .weight(30.5)
                .build();

        MvcResult createResult = mockMvc.perform(post("/api/pets?ownerId=" + ownerId)
                .header("Authorization", "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(petDto)))
                .andReturn();

        Long petId = objectMapper.readTree(createResult.getResponse().getContentAsString())
                .get("id").asLong();

        // Delete pet
        mockMvc.perform(delete("/api/pets/" + petId)
                .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isNoContent());

        // Verify pet is deleted
        mockMvc.perform(get("/api/pets/" + petId)
                .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().is5xxServerError());
    }

    @Test
    @DisplayName("Should fail to create pet without authentication")
    void testCreatePetWithoutAuth() throws Exception {
        PetDto petDto = PetDto.builder()
                .name("Buddy")
                .species("Dog")
                .build();

        mockMvc.perform(post("/api/pets?ownerId=" + ownerId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(petDto)))
                .andExpect(status().isForbidden());
    }
}
