package com.healthytom.controller;

import com.healthytom.dto.PetDto;
import com.healthytom.service.PetService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/pets")
@CrossOrigin(origins = "http://localhost:5173")
@Slf4j
@RequiredArgsConstructor
public class PetController {

    private final PetService petService;

    @PostMapping
    @PreAuthorize("hasRole('OWNER') or hasRole('VETERINARIAN')")
    public ResponseEntity<PetDto> createPet(@RequestBody PetDto petDto, @RequestParam Long ownerId) {
        log.info("Creating pet for owner: {}", ownerId);
        PetDto createdPet = petService.createPet(petDto, ownerId);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdPet);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('OWNER') or hasRole('VETERINARIAN')")
    public ResponseEntity<PetDto> getPetById(@PathVariable Long id) {
        log.info("Fetching pet with id: {}", id);
        PetDto pet = petService.getPetById(id);
        return ResponseEntity.ok(pet);
    }

    @GetMapping("/owner/{ownerId}")
    @PreAuthorize("hasRole('OWNER') or hasRole('VETERINARIAN')")
    public ResponseEntity<List<PetDto>> getPetsByOwnerId(@PathVariable Long ownerId) {
        log.info("Fetching pets for owner: {}", ownerId);
        List<PetDto> pets = petService.getPetsByOwnerId(ownerId);
        return ResponseEntity.ok(pets);
    }

    @GetMapping
    @PreAuthorize("hasRole('VETERINARIAN') or hasRole('ADMIN')")
    public ResponseEntity<List<PetDto>> getAllPets() {
        log.info("Fetching all pets");
        List<PetDto> pets = petService.getAllPets();
        return ResponseEntity.ok(pets);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('OWNER') or hasRole('VETERINARIAN')")
    public ResponseEntity<PetDto> updatePet(@PathVariable Long id, @RequestBody PetDto petDto) {
        log.info("Updating pet with id: {}", id);
        PetDto updatedPet = petService.updatePet(id, petDto);
        return ResponseEntity.ok(updatedPet);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('OWNER') or hasRole('ADMIN')")
    public ResponseEntity<Void> deletePet(@PathVariable Long id) {
        log.info("Deleting pet with id: {}", id);
        petService.deletePet(id);
        return ResponseEntity.noContent().build();
    }
}
