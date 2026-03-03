package com.healthytom.controller;

import com.healthytom.dto.ConsultationDto;
import com.healthytom.service.ConsultationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/consultations")
@CrossOrigin(origins = "*", allowedHeaders = "*")
@Slf4j
@RequiredArgsConstructor
public class ConsultationController {

    private final ConsultationService consultationService;

    @PostMapping
    @PreAuthorize("hasRole('OWNER')")
    public ResponseEntity<ConsultationDto> createConsultation(@RequestBody ConsultationDto consultationDto,
                                                             @RequestParam Long ownerId) {
        log.info("Creating consultation for owner: {}", ownerId);
        ConsultationDto createdConsultation = consultationService.createConsultation(consultationDto, ownerId);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdConsultation);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('OWNER') or hasRole('VETERINARIAN') or hasRole('ADMIN')")
    public ResponseEntity<ConsultationDto> getConsultationById(@PathVariable Long id) {
        log.info("Fetching consultation with id: {}", id);
        ConsultationDto consultation = consultationService.getConsultationById(id);
        return ResponseEntity.ok(consultation);
    }

    @GetMapping("/owner/{ownerId}")
    @PreAuthorize("hasRole('OWNER') or hasRole('ADMIN')")
    public ResponseEntity<List<ConsultationDto>> getConsultationsByOwnerId(@PathVariable Long ownerId) {
        log.info("Fetching consultations for owner: {}", ownerId);
        List<ConsultationDto> consultations = consultationService.getConsultationsByOwnerId(ownerId);
        return ResponseEntity.ok(consultations);
    }

    @GetMapping("/veterinarian/{veterinarianId}")
    @PreAuthorize("hasRole('VETERINARIAN') or hasRole('ADMIN')")
    public ResponseEntity<List<ConsultationDto>> getConsultationsByVeterinarianId(@PathVariable Long veterinarianId) {
        log.info("Fetching consultations for veterinarian: {}", veterinarianId);
        List<ConsultationDto> consultations = consultationService.getConsultationsByVeterinarianId(veterinarianId);
        return ResponseEntity.ok(consultations);
    }

    @GetMapping("/pet/{petId}")
    @PreAuthorize("hasRole('OWNER') or hasRole('VETERINARIAN') or hasRole('ADMIN')")
    public ResponseEntity<List<ConsultationDto>> getConsultationsByPetId(@PathVariable Long petId) {
        log.info("Fetching consultations for pet: {}", petId);
        List<ConsultationDto> consultations = consultationService.getConsultationsByPetId(petId);
        return ResponseEntity.ok(consultations);
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<ConsultationDto>> getAllConsultations() {
        log.info("Fetching all consultations");
        List<ConsultationDto> consultations = consultationService.getAllConsultations();
        return ResponseEntity.ok(consultations);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('VETERINARIAN') or hasRole('ADMIN')")
    public ResponseEntity<ConsultationDto> updateConsultation(@PathVariable Long id,
                                                             @RequestBody ConsultationDto consultationDto) {
        log.info("Updating consultation with id: {}", id);
        ConsultationDto updatedConsultation = consultationService.updateConsultation(id, consultationDto);
        return ResponseEntity.ok(updatedConsultation);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteConsultation(@PathVariable Long id) {
        log.info("Deleting consultation with id: {}", id);
        consultationService.deleteConsultation(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}/assign-veterinarian/{veterinarianId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ConsultationDto> assignVeterinarian(@PathVariable Long id,
                                                             @PathVariable Long veterinarianId) {
        log.info("Assigning veterinarian: {} to consultation: {}", veterinarianId, id);
        ConsultationDto assignedConsultation = consultationService.assignVeterinarian(id, veterinarianId);
        return ResponseEntity.ok(assignedConsultation);
    }

    @PutMapping("/{id}/rate")
    @PreAuthorize("hasRole('OWNER')")
    public ResponseEntity<ConsultationDto> rateConsultation(@PathVariable Long id,
                                                           @RequestParam Float rating,
                                                           @RequestParam(required = false) String feedback) {
        log.info("Rating consultation: {} with rating: {}", id, rating);
        ConsultationDto ratedConsultation = consultationService.rateConsultation(id, rating, feedback);
        return ResponseEntity.ok(ratedConsultation);
    }
}
