package com.healthytom.service;

import com.healthytom.dto.ConsultationDto;
import com.healthytom.entity.Consultation;
import com.healthytom.entity.Pet;
import com.healthytom.entity.User;
import com.healthytom.repository.ConsultationRepository;
import com.healthytom.repository.PetRepository;
import com.healthytom.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ConsultationService Unit Tests")
class ConsultationServiceTest {

    @Mock
    private ConsultationRepository consultationRepository;

    @Mock
    private PetRepository petRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private ConsultationService consultationService;

    private User testOwner;
    private User testVeterinarian;
    private Pet testPet;
    private Consultation testConsultation;
    private ConsultationDto testConsultationDto;

    @BeforeEach
    void setUp() {
        testOwner = User.builder()
                .id(1L)
                .email("owner@example.com")
                .firstName("John")
                .lastName("Doe")
                .role(User.UserRole.OWNER)
                .build();

        testVeterinarian = User.builder()
                .id(2L)
                .email("vet@example.com")
                .firstName("Dr.")
                .lastName("Smith")
                .role(User.UserRole.VETERINARIAN)
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
                .title("Regular Checkup")
                .description("Annual health checkup")
                .symptoms("No symptoms")
                .diagnosis("Healthy")
                .status(Consultation.ConsultationStatus.COMPLETED)
                .consultationDate(LocalDateTime.now())
                .build();

        testConsultationDto = ConsultationDto.builder()
                .id(1L)
                .petId(1L)
                .ownerId(1L)
                .veterinarianId(2L)
                .title("Regular Checkup")
                .description("Annual health checkup")
                .symptoms("No symptoms")
                .status("PENDING")
                .consultationDate(LocalDateTime.now())
                .build();
    }

    @Test
    @DisplayName("Should create a consultation successfully")
    void testCreateConsultationSuccess() {
        // Arrange
        when(petRepository.findById(1L)).thenReturn(Optional.of(testPet));
        when(userRepository.findById(1L)).thenReturn(Optional.of(testOwner));
        when(consultationRepository.save(any(Consultation.class))).thenReturn(testConsultation);

        // Act
        ConsultationDto result = consultationService.createConsultation(testConsultationDto, 1L);

        // Assert
        assertNotNull(result);
        assertEquals("Regular Checkup", result.getTitle());
        assertEquals(1L, result.getPetId());
        verify(consultationRepository, times(1)).save(any(Consultation.class));
    }

    @Test
    @DisplayName("Should throw exception when pet not found")
    void testCreateConsultationWithNonExistentPet() {
        // Arrange
        when(petRepository.findById(anyLong())).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(RuntimeException.class, () -> 
                consultationService.createConsultation(testConsultationDto, 1L));
        verify(consultationRepository, never()).save(any(Consultation.class));
    }

    @Test
    @DisplayName("Should get consultation by ID successfully")
    void testGetConsultationByIdSuccess() {
        // Arrange
        when(consultationRepository.findById(1L)).thenReturn(Optional.of(testConsultation));

        // Act
        ConsultationDto result = consultationService.getConsultationById(1L);

        // Assert
        assertNotNull(result);
        assertEquals("Regular Checkup", result.getTitle());
        assertEquals(1L, result.getId());
    }

    @Test
    @DisplayName("Should throw exception when consultation not found")
    void testGetConsultationByIdNotFound() {
        // Arrange
        when(consultationRepository.findById(anyLong())).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(RuntimeException.class, () -> consultationService.getConsultationById(99L));
    }

    @Test
    @DisplayName("Should get consultations by owner ID")
    void testGetConsultationsByOwnerId() {
        // Arrange
        Consultation consultation2 = Consultation.builder()
                .id(2L)
                .pet(testPet)
                .owner(testOwner)
                .title("Follow-up")
                .status(Consultation.ConsultationStatus.PENDING)
                .build();

        when(consultationRepository.findByOwnerId(1L)).thenReturn(Arrays.asList(testConsultation, consultation2));

        // Act
        List<ConsultationDto> result = consultationService.getConsultationsByOwnerId(1L);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("Regular Checkup", result.get(0).getTitle());
        assertEquals("Follow-up", result.get(1).getTitle());
    }

    @Test
    @DisplayName("Should assign veterinarian to consultation")
    void testAssignVeterinarianSuccess() {
        // Arrange
        when(consultationRepository.findById(1L)).thenReturn(Optional.of(testConsultation));
        when(userRepository.findById(2L)).thenReturn(Optional.of(testVeterinarian));
        when(consultationRepository.save(any(Consultation.class))).thenReturn(testConsultation);

        // Act
        ConsultationDto result = consultationService.assignVeterinarian(1L, 2L);

        // Assert
        assertNotNull(result);
        assertEquals(2L, result.getVeterinarianId());
        verify(consultationRepository, times(1)).save(any(Consultation.class));
    }

    @Test
    @DisplayName("Should rate consultation successfully")
    void testRateConsultationSuccess() {
        // Arrange
        when(consultationRepository.findById(1L)).thenReturn(Optional.of(testConsultation));
        when(consultationRepository.save(any(Consultation.class))).thenReturn(testConsultation);

        // Act
        ConsultationDto result = consultationService.rateConsultation(1L, 5.0f, "Excellent service");

        // Assert
        assertNotNull(result);
        verify(consultationRepository, times(1)).save(any(Consultation.class));
    }

    @Test
    @DisplayName("Should update consultation successfully")
    void testUpdateConsultationSuccess() {
        // Arrange
        ConsultationDto updateDto = ConsultationDto.builder()
                .title("Updated Checkup")
                .status("COMPLETED")
                .diagnosis("Healthy, no issues")
                .build();

        when(consultationRepository.findById(1L)).thenReturn(Optional.of(testConsultation));
        when(consultationRepository.save(any(Consultation.class))).thenReturn(testConsultation);

        // Act
        ConsultationDto result = consultationService.updateConsultation(1L, updateDto);

        // Assert
        assertNotNull(result);
        verify(consultationRepository, times(1)).save(any(Consultation.class));
    }

    @Test
    @DisplayName("Should delete consultation successfully")
    void testDeleteConsultationSuccess() {
        // Arrange
        when(consultationRepository.findById(1L)).thenReturn(Optional.of(testConsultation));

        // Act
        consultationService.deleteConsultation(1L);

        // Assert
        verify(consultationRepository, times(1)).delete(testConsultation);
    }
}
