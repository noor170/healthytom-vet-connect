package com.healthytom.controller;

import com.healthytom.dto.PrescriptionDto;
import com.healthytom.service.PrescriptionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/prescriptions")
@CrossOrigin(origins = "http://localhost:5173")
@Slf4j
@RequiredArgsConstructor
public class PrescriptionController {

    private final PrescriptionService prescriptionService;

    @PostMapping
    @PreAuthorize("hasRole('VETERINARIAN')")
    public ResponseEntity<PrescriptionDto> createPrescription(@RequestBody PrescriptionDto prescriptionDto) {
        log.info("Creating prescription for consultation: {}", prescriptionDto.getConsultationId());
        PrescriptionDto createdPrescription = prescriptionService.createPrescription(prescriptionDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdPrescription);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('OWNER') or hasRole('VETERINARIAN') or hasRole('ADMIN')")
    public ResponseEntity<PrescriptionDto> getPrescriptionById(@PathVariable Long id) {
        log.info("Fetching prescription with id: {}", id);
        PrescriptionDto prescription = prescriptionService.getPrescriptionById(id);
        return ResponseEntity.ok(prescription);
    }

    @GetMapping("/consultation/{consultationId}")
    @PreAuthorize("hasRole('OWNER') or hasRole('VETERINARIAN') or hasRole('ADMIN')")
    public ResponseEntity<List<PrescriptionDto>> getPrescriptionsByConsultationId(@PathVariable Long consultationId) {
        log.info("Fetching prescriptions for consultation: {}", consultationId);
        List<PrescriptionDto> prescriptions = prescriptionService.getPrescriptionsByConsultationId(consultationId);
        return ResponseEntity.ok(prescriptions);
    }

    @GetMapping("/pet/{petId}")
    @PreAuthorize("hasRole('OWNER') or hasRole('VETERINARIAN') or hasRole('ADMIN')")
    public ResponseEntity<List<PrescriptionDto>> getPrescriptionsByPetId(@PathVariable Long petId) {
        log.info("Fetching prescriptions for pet: {}", petId);
        List<PrescriptionDto> prescriptions = prescriptionService.getPrescriptionsByPetId(petId);
        return ResponseEntity.ok(prescriptions);
    }

    @GetMapping("/veterinarian/{veterinarianId}")
    @PreAuthorize("hasRole('VETERINARIAN') or hasRole('ADMIN')")
    public ResponseEntity<List<PrescriptionDto>> getPrescriptionsByVeterinarianId(@PathVariable Long veterinarianId) {
        log.info("Fetching prescriptions for veterinarian: {}", veterinarianId);
        List<PrescriptionDto> prescriptions = prescriptionService.getPrescriptionsByVeterinarianId(veterinarianId);
        return ResponseEntity.ok(prescriptions);
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<PrescriptionDto>> getAllPrescriptions() {
        log.info("Fetching all prescriptions");
        List<PrescriptionDto> prescriptions = prescriptionService.getAllPrescriptions();
        return ResponseEntity.ok(prescriptions);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('VETERINARIAN') or hasRole('ADMIN')")
    public ResponseEntity<PrescriptionDto> updatePrescription(@PathVariable Long id,
                                                             @RequestBody PrescriptionDto prescriptionDto) {
        log.info("Updating prescription with id: {}", id);
        PrescriptionDto updatedPrescription = prescriptionService.updatePrescription(id, prescriptionDto);
        return ResponseEntity.ok(updatedPrescription);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('VETERINARIAN') or hasRole('ADMIN')")
    public ResponseEntity<Void> deletePrescription(@PathVariable Long id) {
        log.info("Deleting prescription with id: {}", id);
        prescriptionService.deletePrescription(id);
        return ResponseEntity.noContent().build();
    }
}
