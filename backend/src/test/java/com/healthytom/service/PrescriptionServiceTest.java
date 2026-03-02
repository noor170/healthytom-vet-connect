package com.healthytom.service;

import com.healthytom.dto.PrescriptionDto;
import com.healthytom.entity.Consultation;
import com.healthytom.entity.Pet;
import com.healthytom.entity.Prescription;
import com.healthytom.entity.User;
import com.healthytom.repository.ConsultationRepository;
import com.healthytom.repository.PetRepository;
import com.healthytom.repository.PrescriptionRepository;
import com.healthytom.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("PrescriptionService Unit Tests")
class PrescriptionServiceTest {

    @Mock
    private PrescriptionRepository prescriptionRepository;

    @Mock
    private ConsultationRepository consultationRepository;

    @Mock
    private PetRepository petRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private PrescriptionService prescriptionService;

    private User testVeterinarian;
    private User testOwner;
    private Pet testPet;
    private Consultation testConsultation;
    private Prescription testPrescription;
    private PrescriptionDto testPrescriptionDto;

    @BeforeEach
    void setUp() {
        testVeterinarian = User.builder()
                .id(2L)
                .email("vet@example.com")
                .firstName("Dr.")
                .lastName("Smith")
                .role(User.UserRole.VETERINARIAN)
                .build();

        testOwner = User.builder()
                .id(1L)
                .email("owner@example.com")
                .firstName("John")
                .lastName("Doe")
                .role(User.UserRole.OWNER)
                .build();

        testPet = Pet.builder()
                .id(1L)
                .name("Buddy")
                .species("Dog")
                .owner(testOwner)
                .build();

        testConsultation = Consultation.builder()
                .id(1L)
                .pet(testPet)
                .owner(testOwner)
                .veterinarian(testVeterinarian)
                .title("Checkup")
                .build();

        testPrescription = Prescription.builder()
                .id(1L)
                .consultation(testConsultation)
                .pet(testPet)
                .veterinarian(testVeterinarian)
                .medicationName("Amoxicillin")
                .dosage("250 mg")
                .frequency("twice daily")
                .duration(7)
                .status(Prescription.PrescriptionStatus.ACTIVE)
                .prescribedDate(LocalDate.now())
                .build();

        testPrescriptionDto = PrescriptionDto.builder()
                .id(1L)
                .consultationId(1L)
                .petId(1L)
                .veterinarianId(2L)
                .medicationName("Amoxicillin")
                .dosage("250 mg")
                .frequency("twice daily")
                .duration(7)
                .status("ACTIVE")
                .prescribedDate(LocalDate.now())
                .build();
    }

    @Test
    @DisplayName("Should create a prescription successfully")
    void testCreatePrescriptionSuccess() {
        // Arrange
        when(consultationRepository.findById(1L)).thenReturn(Optional.of(testConsultation));
        when(petRepository.findById(1L)).thenReturn(Optional.of(testPet));
        when(userRepository.findById(2L)).thenReturn(Optional.of(testVeterinarian));
        when(prescriptionRepository.save(any(Prescription.class))).thenReturn(testPrescription);

        // Act
        PrescriptionDto result = prescriptionService.createPrescription(testPrescriptionDto);

        // Assert
        assertNotNull(result);
        assertEquals("Amoxicillin", result.getMedicationName());
        assertEquals("250 mg", result.getDosage());
        assertEquals(2L, result.getVeterinarianId());
        verify(prescriptionRepository, times(1)).save(any(Prescription.class));
    }

    @Test
    @DisplayName("Should throw exception when consultation not found")
    void testCreatePrescriptionWithNonExistentConsultation() {
        // Arrange
        when(consultationRepository.findById(anyLong())).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(RuntimeException.class, () -> 
                prescriptionService.createPrescription(testPrescriptionDto));
        verify(prescriptionRepository, never()).save(any(Prescription.class));
    }

    @Test
    @DisplayName("Should get prescription by ID successfully")
    void testGetPrescriptionByIdSuccess() {
        // Arrange
        when(prescriptionRepository.findById(1L)).thenReturn(Optional.of(testPrescription));

        // Act
        PrescriptionDto result = prescriptionService.getPrescriptionById(1L);

        // Assert
        assertNotNull(result);
        assertEquals("Amoxicillin", result.getMedicationName());
        assertEquals(1L, result.getId());
    }

    @Test
    @DisplayName("Should throw exception when prescription not found")
    void testGetPrescriptionByIdNotFound() {
        // Arrange
        when(prescriptionRepository.findById(anyLong())).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(RuntimeException.class, () -> prescriptionService.getPrescriptionById(99L));
    }

    @Test
    @DisplayName("Should get prescriptions by consultation ID")
    void testGetPrescriptionsByConsultationId() {
        // Arrange
        Prescription prescription2 = Prescription.builder()
                .id(2L)
                .consultation(testConsultation)
                .pet(testPet)
                .veterinarian(testVeterinarian)
                .medicationName("Ibuprofen")
                .dosage("200 mg")
                .status(Prescription.PrescriptionStatus.ACTIVE)
                .build();

        when(prescriptionRepository.findByConsultationId(1L))
                .thenReturn(Arrays.asList(testPrescription, prescription2));

        // Act
        List<PrescriptionDto> result = prescriptionService.getPrescriptionsByConsultationId(1L);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("Amoxicillin", result.get(0).getMedicationName());
        assertEquals("Ibuprofen", result.get(1).getMedicationName());
    }

    @Test
    @DisplayName("Should get prescriptions by pet ID")
    void testGetPrescriptionsByPetId() {
        // Arrange
        when(prescriptionRepository.findByPetId(1L)).thenReturn(Arrays.asList(testPrescription));

        // Act
        List<PrescriptionDto> result = prescriptionService.getPrescriptionsByPetId(1L);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Amoxicillin", result.get(0).getMedicationName());
    }

    @Test
    @DisplayName("Should get prescriptions by veterinarian ID")
    void testGetPrescriptionsByVeterinarianId() {
        // Arrange
        when(prescriptionRepository.findByVeterinarianId(2L)).thenReturn(Arrays.asList(testPrescription));

        // Act
        List<PrescriptionDto> result = prescriptionService.getPrescriptionsByVeterinarianId(2L);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(2L, result.get(0).getVeterinarianId());
    }

    @Test
    @DisplayName("Should update prescription successfully")
    void testUpdatePrescriptionSuccess() {
        // Arrange
        PrescriptionDto updateDto = PrescriptionDto.builder()
                .medicationName("Amoxicillin Updated")
                .dosage("500 mg")
                .status("COMPLETED")
                .build();

        when(prescriptionRepository.findById(1L)).thenReturn(Optional.of(testPrescription));
        when(prescriptionRepository.save(any(Prescription.class))).thenReturn(testPrescription);

        // Act
        PrescriptionDto result = prescriptionService.updatePrescription(1L, updateDto);

        // Assert
        assertNotNull(result);
        verify(prescriptionRepository, times(1)).save(any(Prescription.class));
    }

    @Test
    @DisplayName("Should delete prescription successfully")
    void testDeletePrescriptionSuccess() {
        // Arrange
        when(prescriptionRepository.findById(1L)).thenReturn(Optional.of(testPrescription));

        // Act
        prescriptionService.deletePrescription(1L);

        // Assert
        verify(prescriptionRepository, times(1)).delete(testPrescription);
    }

    @Test
    @DisplayName("Should throw exception when deleting non-existent prescription")
    void testDeletePrescriptionNotFound() {
        // Arrange
        when(prescriptionRepository.findById(anyLong())).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(RuntimeException.class, () -> prescriptionService.deletePrescription(99L));
        verify(prescriptionRepository, never()).delete(any(Prescription.class));
    }
}
