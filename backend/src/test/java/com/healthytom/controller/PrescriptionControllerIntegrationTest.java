package com.healthytom.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.healthytom.dto.ConsultationDto;
import com.healthytom.dto.PetDto;
import com.healthytom.dto.PrescriptionDto;
import com.healthytom.dto.RegisterRequest;
import com.healthytom.repository.ConsultationRepository;
import com.healthytom.repository.PetRepository;
import com.healthytom.repository.PrescriptionRepository;
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
import java.time.LocalDateTime;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("dev")
@DisplayName("PrescriptionController Integration Tests")
class PrescriptionControllerIntegrationTest {

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

    @Autowired
    private PrescriptionRepository prescriptionRepository;

    private String ownerAccessToken;
    private String vetAccessToken;
    private Long ownerId;
    private Long vetId;
    private Long petId;
    private Long consultationId;

    @BeforeEach
    @Transactional
    void setUp() throws Exception {
        prescriptionRepository.deleteAll();
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

        // Create consultation
        ConsultationDto consultationDto = ConsultationDto.builder()
                .petId(petId)
                .title("Regular Checkup")
                .status("PENDING")
                .consultationDate(LocalDateTime.now().plusDays(1))
                .build();

        MvcResult consultationResult = mockMvc.perform(post("/api/consultations?ownerId=" + ownerId)
                .header("Authorization", "Bearer " + ownerAccessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(consultationDto)))
                .andReturn();

        consultationId = objectMapper.readTree(consultationResult.getResponse().getContentAsString())
                .get("id").asLong();
    }

    @Test
    @DisplayName("Should create a prescription successfully")
    void testCreatePrescriptionSuccess() throws Exception {
        PrescriptionDto prescriptionDto = PrescriptionDto.builder()
                .consultationId(consultationId)
                .petId(petId)
                .veterinarianId(vetId)
                .medicationName("Amoxicillin")
                .dosage("250 mg")
                .frequency("twice daily")
                .duration(7)
                .instructions("Take with food")
                .status("ACTIVE")
                .prescribedDate(LocalDate.now())
                .startDate(LocalDate.now())
                .endDate(LocalDate.now().plusDays(7))
                .build();

        mockMvc.perform(post("/api/prescriptions")
                .header("Authorization", "Bearer " + vetAccessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(prescriptionDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.medicationName").value("Amoxicillin"))
                .andExpect(jsonPath("$.dosage").value("250 mg"))
                .andExpect(jsonPath("$.petId").value(petId));
    }

    @Test
    @DisplayName("Should get prescription by ID")
    @Transactional
    void testGetPrescriptionById() throws Exception {
        // Create prescription
        PrescriptionDto prescriptionDto = PrescriptionDto.builder()
                .consultationId(consultationId)
                .petId(petId)
                .veterinarianId(vetId)
                .medicationName("Amoxicillin")
                .dosage("250 mg")
                .status("ACTIVE")
                .prescribedDate(LocalDate.now())
                .build();

        MvcResult createResult = mockMvc.perform(post("/api/prescriptions")
                .header("Authorization", "Bearer " + vetAccessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(prescriptionDto)))
                .andReturn();

        Long prescriptionId = objectMapper.readTree(createResult.getResponse().getContentAsString())
                .get("id").asLong();

        // Get prescription
        mockMvc.perform(get("/api/prescriptions/" + prescriptionId)
                .header("Authorization", "Bearer " + vetAccessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(prescriptionId))
                .andExpect(jsonPath("$.medicationName").value("Amoxicillin"));
    }

    @Test
    @DisplayName("Should get prescriptions by consultation")
    @Transactional
    void testGetPrescriptionsByConsultation() throws Exception {
        // Create prescription
        PrescriptionDto prescriptionDto = PrescriptionDto.builder()
                .consultationId(consultationId)
                .petId(petId)
                .veterinarianId(vetId)
                .medicationName("Amoxicillin")
                .dosage("250 mg")
                .status("ACTIVE")
                .prescribedDate(LocalDate.now())
                .build();

        mockMvc.perform(post("/api/prescriptions")
                .header("Authorization", "Bearer " + vetAccessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(prescriptionDto)));

        // Get prescriptions
        mockMvc.perform(get("/api/prescriptions/consultation/" + consultationId)
                .header("Authorization", "Bearer " + vetAccessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].medicationName").value("Amoxicillin"));
    }

    @Test
    @DisplayName("Should get prescriptions by pet")
    @Transactional
    void testGetPrescriptionsByPet() throws Exception {
        // Create prescription
        PrescriptionDto prescriptionDto = PrescriptionDto.builder()
                .consultationId(consultationId)
                .petId(petId)
                .veterinarianId(vetId)
                .medicationName("Amoxicillin")
                .status("ACTIVE")
                .prescribedDate(LocalDate.now())
                .build();

        mockMvc.perform(post("/api/prescriptions")
                .header("Authorization", "Bearer " + vetAccessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(prescriptionDto)));

        // Get prescriptions
        mockMvc.perform(get("/api/prescriptions/pet/" + petId)
                .header("Authorization", "Bearer " + vetAccessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    @DisplayName("Should get prescriptions by veterinarian")
    @Transactional
    void testGetPrescriptionsByVeterinarian() throws Exception {
        // Create prescription
        PrescriptionDto prescriptionDto = PrescriptionDto.builder()
                .consultationId(consultationId)
                .petId(petId)
                .veterinarianId(vetId)
                .medicationName("Amoxicillin")
                .status("ACTIVE")
                .prescribedDate(LocalDate.now())
                .build();

        mockMvc.perform(post("/api/prescriptions")
                .header("Authorization", "Bearer " + vetAccessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(prescriptionDto)));

        // Get prescriptions
        mockMvc.perform(get("/api/prescriptions/veterinarian/" + vetId)
                .header("Authorization", "Bearer " + vetAccessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    @DisplayName("Should update prescription successfully")
    @Transactional
    void testUpdatePrescriptionSuccess() throws Exception {
        // Create prescription
        PrescriptionDto prescriptionDto = PrescriptionDto.builder()
                .consultationId(consultationId)
                .petId(petId)
                .veterinarianId(vetId)
                .medicationName("Amoxicillin")
                .dosage("250 mg")
                .status("ACTIVE")
                .prescribedDate(LocalDate.now())
                .build();

        MvcResult createResult = mockMvc.perform(post("/api/prescriptions")
                .header("Authorization", "Bearer " + vetAccessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(prescriptionDto)))
                .andReturn();

        Long prescriptionId = objectMapper.readTree(createResult.getResponse().getContentAsString())
                .get("id").asLong();

        // Update prescription
        PrescriptionDto updateDto = PrescriptionDto.builder()
                .consultationId(consultationId)
                .petId(petId)
                .veterinarianId(vetId)
                .medicationName("Amoxicillin Updated")
                .dosage("500 mg")
                .status("COMPLETED")
                .build();

        mockMvc.perform(put("/api/prescriptions/" + prescriptionId)
                .header("Authorization", "Bearer " + vetAccessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(prescriptionId));
    }

    @Test
    @DisplayName("Should delete prescription successfully")
    @Transactional
    void testDeletePrescriptionSuccess() throws Exception {
        // Create prescription
        PrescriptionDto prescriptionDto = PrescriptionDto.builder()
                .consultationId(consultationId)
                .petId(petId)
                .veterinarianId(vetId)
                .medicationName("Amoxicillin")
                .status("ACTIVE")
                .prescribedDate(LocalDate.now())
                .build();

        MvcResult createResult = mockMvc.perform(post("/api/prescriptions")
                .header("Authorization", "Bearer " + vetAccessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(prescriptionDto)))
                .andReturn();

        Long prescriptionId = objectMapper.readTree(createResult.getResponse().getContentAsString())
                .get("id").asLong();

        // Delete prescription
        mockMvc.perform(delete("/api/prescriptions/" + prescriptionId)
                .header("Authorization", "Bearer " + vetAccessToken))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("Should fail to create prescription without veterinarian role")
    void testCreatePrescriptionWithoutVetRole() throws Exception {
        PrescriptionDto prescriptionDto = PrescriptionDto.builder()
                .consultationId(consultationId)
                .petId(petId)
                .veterinarianId(vetId)
                .medicationName("Amoxicillin")
                .status("ACTIVE")
                .build();

        // Owner trying to create prescription
        mockMvc.perform(post("/api/prescriptions")
                .header("Authorization", "Bearer " + ownerAccessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(prescriptionDto)))
                .andExpect(status().isForbidden());
    }
}
