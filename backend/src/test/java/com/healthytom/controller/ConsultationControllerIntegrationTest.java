package com.healthytom.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.healthytom.dto.ConsultationDto;
import com.healthytom.dto.PetDto;
import com.healthytom.dto.RegisterRequest;
import com.healthytom.repository.ConsultationRepository;
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

import java.time.LocalDateTime;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("dev")
@DisplayName("ConsultationController Integration Tests")
class ConsultationControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PetRepository petRepository;

    @Autowired
    private ConsultationRepository consultationRepository;

    private String ownerAccessToken;
    private String vetAccessToken;
    private Long ownerId;
    private Long vetId;
    private Long petId;

    @BeforeEach
    @Transactional
    void setUp() throws Exception {
        consultationRepository.deleteAll();
        petRepository.deleteAll();
        userRepository.deleteAll();

        // Register owner
        RegisterRequest ownerRequest = RegisterRequest.builder()
                .email("owner@example.com")
                .password("Password@123")
                .firstName("John")
                .lastName("Doe")
                .role("OWNER")
                .build();

        MvcResult ownerResult = mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(ownerRequest)))
                .andReturn();

        String ownerResponse = ownerResult.getResponse().getContentAsString();
        ownerAccessToken = objectMapper.readTree(ownerResponse).get("accessToken").asText();
        ownerId = objectMapper.readTree(ownerResponse).get("user").get("id").asLong();

        // Register veterinarian
        RegisterRequest vetRequest = RegisterRequest.builder()
                .email("vet@example.com")
                .password("Password@123")
                .firstName("Dr.")
                .lastName("Smith")
                .role("VETERINARIAN")
                .specialization("Small Animals")
                .licenseNumber("VET123456")
                .build();

        MvcResult vetResult = mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(vetRequest)))
                .andReturn();

        String vetResponse = vetResult.getResponse().getContentAsString();
        vetAccessToken = objectMapper.readTree(vetResponse).get("accessToken").asText();
        vetId = objectMapper.readTree(vetResponse).get("user").get("id").asLong();

        // Create pet
        PetDto petDto = PetDto.builder()
                .name("Buddy")
                .species("Dog")
                .breed("Golden Retriever")
                .build();

        MvcResult petResult = mockMvc.perform(post("/api/pets?ownerId=" + ownerId)
                .header("Authorization", "Bearer " + ownerAccessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(petDto)))
                .andReturn();

        petId = objectMapper.readTree(petResult.getResponse().getContentAsString())
                .get("id").asLong();
    }

    @Test
    @DisplayName("Should create a consultation successfully")
    void testCreateConsultationSuccess() throws Exception {
        ConsultationDto consultationDto = ConsultationDto.builder()
                .petId(petId)
                .title("Regular Checkup")
                .description("Annual health checkup")
                .symptoms("No symptoms")
                .status("PENDING")
                .consultationDate(LocalDateTime.now().plusDays(1))
                .build();

        mockMvc.perform(post("/api/consultations?ownerId=" + ownerId)
                .header("Authorization", "Bearer " + ownerAccessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(consultationDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.title").value("Regular Checkup"))
                .andExpect(jsonPath("$.petId").value(petId));
    }

    @Test
    @DisplayName("Should get consultation by ID")
    @Transactional
    void testGetConsultationById() throws Exception {
        // Create consultation
        ConsultationDto consultationDto = ConsultationDto.builder()
                .petId(petId)
                .title("Regular Checkup")
                .description("Annual health checkup")
                .symptoms("No symptoms")
                .status("PENDING")
                .consultationDate(LocalDateTime.now().plusDays(1))
                .build();

        MvcResult createResult = mockMvc.perform(post("/api/consultations?ownerId=" + ownerId)
                .header("Authorization", "Bearer " + ownerAccessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(consultationDto)))
                .andReturn();

        Long consultationId = objectMapper.readTree(createResult.getResponse().getContentAsString())
                .get("id").asLong();

        // Get consultation
        mockMvc.perform(get("/api/consultations/" + consultationId)
                .header("Authorization", "Bearer " + ownerAccessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(consultationId))
                .andExpect(jsonPath("$.title").value("Regular Checkup"));
    }

    @Test
    @DisplayName("Should get consultations by owner")
    @Transactional
    void testGetConsultationsByOwner() throws Exception {
        // Create consultation
        ConsultationDto consultationDto = ConsultationDto.builder()
                .petId(petId)
                .title("Regular Checkup")
                .status("PENDING")
                .consultationDate(LocalDateTime.now().plusDays(1))
                .build();

        mockMvc.perform(post("/api/consultations?ownerId=" + ownerId)
                .header("Authorization", "Bearer " + ownerAccessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(consultationDto)));

        // Get owner's consultations
        mockMvc.perform(get("/api/consultations/owner/" + ownerId)
                .header("Authorization", "Bearer " + ownerAccessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].title").value("Regular Checkup"));
    }

    @Test
    @DisplayName("Should assign veterinarian to consultation")
    @Transactional
    void testAssignVeterinarian() throws Exception {
        // Create consultation
        ConsultationDto consultationDto = ConsultationDto.builder()
                .petId(petId)
                .title("Regular Checkup")
                .status("PENDING")
                .consultationDate(LocalDateTime.now().plusDays(1))
                .build();

        MvcResult createResult = mockMvc.perform(post("/api/consultations?ownerId=" + ownerId)
                .header("Authorization", "Bearer " + ownerAccessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(consultationDto)))
                .andReturn();

        Long consultationId = objectMapper.readTree(createResult.getResponse().getContentAsString())
                .get("id").asLong();

        // Assign veterinarian (using owner token, should fail - need admin/vet)
        mockMvc.perform(put("/api/consultations/" + consultationId + "/assign-veterinarian/" + vetId)
                .header("Authorization", "Bearer " + ownerAccessToken))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Should rate consultation successfully")
    @Transactional
    void testRateConsultation() throws Exception {
        // Create consultation
        ConsultationDto consultationDto = ConsultationDto.builder()
                .petId(petId)
                .title("Regular Checkup")
                .status("PENDING")
                .consultationDate(LocalDateTime.now().plusDays(1))
                .build();

        MvcResult createResult = mockMvc.perform(post("/api/consultations?ownerId=" + ownerId)
                .header("Authorization", "Bearer " + ownerAccessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(consultationDto)))
                .andReturn();

        Long consultationId = objectMapper.readTree(createResult.getResponse().getContentAsString())
                .get("id").asLong();

        // Rate consultation
        mockMvc.perform(put("/api/consultations/" + consultationId + "/rate?rating=5&feedback=Excellent")
                .header("Authorization", "Bearer " + ownerAccessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.rating").value(5.0));
    }

    @Test
    @DisplayName("Should fail to create consultation without authentication")
    void testCreateConsultationWithoutAuth() throws Exception {
        ConsultationDto consultationDto = ConsultationDto.builder()
                .petId(petId)
                .title("Regular Checkup")
                .status("PENDING")
                .build();

        mockMvc.perform(post("/api/consultations?ownerId=" + ownerId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(consultationDto)))
                .andExpect(status().isForbidden());
    }
}
