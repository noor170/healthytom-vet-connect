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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class PrescriptionService {

    private final PrescriptionRepository prescriptionRepository;
    private final ConsultationRepository consultationRepository;
    private final PetRepository petRepository;
    private final UserRepository userRepository;

    @Transactional
    public PrescriptionDto createPrescription(PrescriptionDto dto) {
        Consultation consultation = consultationRepository.findById(dto.getConsultationId())
                .orElseThrow(() -> new RuntimeException("Consultation not found"));

        Pet pet = petRepository.findById(dto.getPetId())
                .orElseThrow(() -> new RuntimeException("Pet not found"));

        User veterinarian = userRepository.findById(dto.getVeterinarianId())
                .orElseThrow(() -> new RuntimeException("Veterinarian not found"));

        Prescription prescription = Prescription.builder()
                .consultation(consultation)
                .pet(pet)
                .veterinarian(veterinarian)
                .medicationName(dto.getMedicationName())
                .dosage(dto.getDosage())
                .frequency(dto.getFrequency())
                .duration(dto.getDuration())
                .instructions(dto.getInstructions())
                .sideEffects(dto.getSideEffects())
                .status(Prescription.PrescriptionStatus.valueOf(dto.getStatus().toUpperCase()))
                .prescribedDate(dto.getPrescribedDate())
                .startDate(dto.getStartDate())
                .endDate(dto.getEndDate())
                .notes(dto.getNotes())
                .build();

        Prescription savedPrescription = prescriptionRepository.save(prescription);
        log.info("Prescription created with id: {} for pet: {}", savedPrescription.getId(), pet.getId());
        return convertToDto(savedPrescription);
    }

    @Transactional(readOnly = true)
    public PrescriptionDto getPrescriptionById(Long id) {
        Prescription prescription = prescriptionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Prescription not found"));
        return convertToDto(prescription);
    }

    @Transactional(readOnly = true)
    public List<PrescriptionDto> getPrescriptionsByConsultationId(Long consultationId) {
        return prescriptionRepository.findByConsultationId(consultationId).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<PrescriptionDto> getPrescriptionsByPetId(Long petId) {
        return prescriptionRepository.findByPetId(petId).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<PrescriptionDto> getPrescriptionsByVeterinarianId(Long veterinarianId) {
        return prescriptionRepository.findByVeterinarianId(veterinarianId).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<PrescriptionDto> getAllPrescriptions() {
        return prescriptionRepository.findAll().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public PrescriptionDto updatePrescription(Long id, PrescriptionDto dto) {
        Prescription prescription = prescriptionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Prescription not found"));

        prescription.setMedicationName(dto.getMedicationName());
        prescription.setDosage(dto.getDosage());
        prescription.setFrequency(dto.getFrequency());
        prescription.setDuration(dto.getDuration());
        prescription.setInstructions(dto.getInstructions());
        prescription.setSideEffects(dto.getSideEffects());
        prescription.setStatus(Prescription.PrescriptionStatus.valueOf(dto.getStatus().toUpperCase()));
        prescription.setPrescribedDate(dto.getPrescribedDate());
        prescription.setStartDate(dto.getStartDate());
        prescription.setEndDate(dto.getEndDate());
        prescription.setNotes(dto.getNotes());

        Prescription updatedPrescription = prescriptionRepository.save(prescription);
        log.info("Prescription updated with id: {}", id);
        return convertToDto(updatedPrescription);
    }

    @Transactional
    public void deletePrescription(Long id) {
        Prescription prescription = prescriptionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Prescription not found"));
        prescriptionRepository.delete(prescription);
        log.info("Prescription deleted with id: {}", id);
    }

    private PrescriptionDto convertToDto(Prescription prescription) {
        return PrescriptionDto.builder()
                .id(prescription.getId())
                .consultationId(prescription.getConsultation().getId())
                .petId(prescription.getPet().getId())
                .veterinarianId(prescription.getVeterinarian().getId())
                .medicationName(prescription.getMedicationName())
                .dosage(prescription.getDosage())
                .frequency(prescription.getFrequency())
                .duration(prescription.getDuration())
                .instructions(prescription.getInstructions())
                .sideEffects(prescription.getSideEffects())
                .status(prescription.getStatus().name())
                .prescribedDate(prescription.getPrescribedDate())
                .startDate(prescription.getStartDate())
                .endDate(prescription.getEndDate())
                .notes(prescription.getNotes())
                .build();
    }
}
