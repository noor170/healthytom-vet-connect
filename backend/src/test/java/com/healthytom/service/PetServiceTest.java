package com.healthytom.service;

import com.healthytom.dto.PetDto;
import com.healthytom.entity.Pet;
import com.healthytom.entity.User;
import com.healthytom.repository.PetRepository;
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
@DisplayName("PetService Unit Tests")
class PetServiceTest {

    @Mock
    private PetRepository petRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private PetService petService;

    private User testOwner;
    private Pet testPet;
    private PetDto testPetDto;

    @BeforeEach
    void setUp() {
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
                .breed("Golden Retriever")
                .color("Golden")
                .weight(30.5)
                .dateOfBirth(LocalDate.of(2020, 1, 15))
                .owner(testOwner)
                .build();

        testPetDto = PetDto.builder()
                .id(1L)
                .name("Buddy")
                .species("Dog")
                .breed("Golden Retriever")
                .color("Golden")
                .weight(30.5)
                .dateOfBirth(LocalDate.of(2020, 1, 15))
                .ownerId(1L)
                .build();
    }

    @Test
    @DisplayName("Should create a pet successfully")
    void testCreatePetSuccess() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.of(testOwner));
        when(petRepository.save(any(Pet.class))).thenReturn(testPet);

        // Act
        PetDto result = petService.createPet(testPetDto, 1L);

        // Assert
        assertNotNull(result);
        assertEquals("Buddy", result.getName());
        assertEquals("Dog", result.getSpecies());
        assertEquals(1L, result.getOwnerId());
        verify(petRepository, times(1)).save(any(Pet.class));
    }

    @Test
    @DisplayName("Should throw exception when owner not found")
    void testCreatePetWithNonExistentOwner() {
        // Arrange
        when(userRepository.findById(anyLong())).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(RuntimeException.class, () -> petService.createPet(testPetDto, 99L));
        verify(petRepository, never()).save(any(Pet.class));
    }

    @Test
    @DisplayName("Should get pet by ID successfully")
    void testGetPetByIdSuccess() {
        // Arrange
        when(petRepository.findById(1L)).thenReturn(Optional.of(testPet));

        // Act
        PetDto result = petService.getPetById(1L);

        // Assert
        assertNotNull(result);
        assertEquals("Buddy", result.getName());
        assertEquals(1L, result.getId());
    }

    @Test
    @DisplayName("Should throw exception when pet not found")
    void testGetPetByIdNotFound() {
        // Arrange
        when(petRepository.findById(anyLong())).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(RuntimeException.class, () -> petService.getPetById(99L));
    }

    @Test
    @DisplayName("Should get all pets by owner ID")
    void testGetPetsByOwnerId() {
        // Arrange
        Pet pet2 = Pet.builder()
                .id(2L)
                .name("Max")
                .species("Cat")
                .breed("Siamese")
                .owner(testOwner)
                .build();

        when(petRepository.findByOwnerId(1L)).thenReturn(Arrays.asList(testPet, pet2));

        // Act
        List<PetDto> result = petService.getPetsByOwnerId(1L);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("Buddy", result.get(0).getName());
        assertEquals("Max", result.get(1).getName());
    }

    @Test
    @DisplayName("Should update pet successfully")
    void testUpdatePetSuccess() {
        // Arrange
        PetDto updateDto = PetDto.builder()
                .name("Buddy Updated")
                .species("Dog")
                .breed("Golden Retriever")
                .weight(31.0)
                .build();

        when(petRepository.findById(1L)).thenReturn(Optional.of(testPet));
        when(petRepository.save(any(Pet.class))).thenReturn(testPet);

        // Act
        PetDto result = petService.updatePet(1L, updateDto);

        // Assert
        assertNotNull(result);
        verify(petRepository, times(1)).save(any(Pet.class));
    }

    @Test
    @DisplayName("Should delete pet successfully")
    void testDeletePetSuccess() {
        // Arrange
        when(petRepository.findById(1L)).thenReturn(Optional.of(testPet));

        // Act
        petService.deletePet(1L);

        // Assert
        verify(petRepository, times(1)).delete(testPet);
    }

    @Test
    @DisplayName("Should throw exception when deleting non-existent pet")
    void testDeletePetNotFound() {
        // Arrange
        when(petRepository.findById(anyLong())).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(RuntimeException.class, () -> petService.deletePet(99L));
        verify(petRepository, never()).delete(any(Pet.class));
    }
}
