package com.healthytom.service;

import com.healthytom.dto.PetDto;
import com.healthytom.entity.Pet;
import com.healthytom.entity.User;
import com.healthytom.repository.PetRepository;
import com.healthytom.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class PetService {

    private final PetRepository petRepository;
    private final UserRepository userRepository;

    @Transactional
    public PetDto createPet(PetDto petDto, Long ownerId) {
        User owner = userRepository.findById(ownerId)
                .orElseThrow(() -> new RuntimeException("Owner not found"));

        Pet pet = Pet.builder()
                .name(petDto.getName())
                .species(petDto.getSpecies())
                .breed(petDto.getBreed())
                .color(petDto.getColor())
                .weight(petDto.getWeight())
                .dateOfBirth(petDto.getDateOfBirth())
                .microchipNumber(petDto.getMicrochipNumber())
                .medicalRecordNumber(petDto.getMedicalRecordNumber())
                .medicalHistory(petDto.getMedicalHistory())
                .vaccinations(petDto.getVaccinations())
                .photoUrl(petDto.getPhotoUrl())
                .owner(owner)
                .build();

        Pet savedPet = petRepository.save(pet);
        log.info("Pet created with id: {} for owner: {}", savedPet.getId(), ownerId);
        return convertToDto(savedPet);
    }

    @Transactional(readOnly = true)
    public PetDto getPetById(Long id) {
        Pet pet = petRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Pet not found"));
        return convertToDto(pet);
    }

    @Transactional(readOnly = true)
    public List<PetDto> getPetsByOwnerId(Long ownerId) {
        return petRepository.findByOwnerId(ownerId).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<PetDto> getAllPets() {
        return petRepository.findAll().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public PetDto updatePet(Long id, PetDto petDto) {
        Pet pet = petRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Pet not found"));

        pet.setName(petDto.getName());
        pet.setSpecies(petDto.getSpecies());
        pet.setBreed(petDto.getBreed());
        pet.setColor(petDto.getColor());
        pet.setWeight(petDto.getWeight());
        pet.setDateOfBirth(petDto.getDateOfBirth());
        pet.setMicrochipNumber(petDto.getMicrochipNumber());
        pet.setMedicalRecordNumber(petDto.getMedicalRecordNumber());
        pet.setMedicalHistory(petDto.getMedicalHistory());
        pet.setVaccinations(petDto.getVaccinations());
        pet.setPhotoUrl(petDto.getPhotoUrl());

        Pet updatedPet = petRepository.save(pet);
        log.info("Pet updated with id: {}", id);
        return convertToDto(updatedPet);
    }

    @Transactional
    public void deletePet(Long id) {
        Pet pet = petRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Pet not found"));
        petRepository.delete(pet);
        log.info("Pet deleted with id: {}", id);
    }

    private PetDto convertToDto(Pet pet) {
        return PetDto.builder()
                .id(pet.getId())
                .name(pet.getName())
                .species(pet.getSpecies())
                .breed(pet.getBreed())
                .color(pet.getColor())
                .weight(pet.getWeight())
                .dateOfBirth(pet.getDateOfBirth())
                .microchipNumber(pet.getMicrochipNumber())
                .medicalRecordNumber(pet.getMedicalRecordNumber())
                .medicalHistory(pet.getMedicalHistory())
                .vaccinations(pet.getVaccinations())
                .photoUrl(pet.getPhotoUrl())
                .ownerId(pet.getOwner().getId())
                .build();
    }
}
