package com.healthytom.service;

import com.healthytom.dto.ConsultationDto;
import com.healthytom.entity.Consultation;
import com.healthytom.entity.Pet;
import com.healthytom.entity.User;
import com.healthytom.repository.ConsultationRepository;
import com.healthytom.repository.PetRepository;
import com.healthytom.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class ConsultationService {

    private final ConsultationRepository consultationRepository;
    private final PetRepository petRepository;
    private final UserRepository userRepository;

    @Transactional
    public ConsultationDto createConsultation(ConsultationDto dto, Long ownerId) {
        Pet pet = petRepository.findById(dto.getPetId())
                .orElseThrow(() -> new RuntimeException("Pet not found"));

        User owner = userRepository.findById(ownerId)
                .orElseThrow(() -> new RuntimeException("Owner not found"));

        Consultation consultation = Consultation.builder()
                .pet(pet)
                .owner(owner)
                .title(dto.getTitle())
                .description(dto.getDescription())
                .symptoms(dto.getSymptoms())
                .status(Consultation.ConsultationStatus.valueOf(dto.getStatus().toUpperCase()))
                .consultationDate(dto.getConsultationDate() != null ? dto.getConsultationDate() : LocalDateTime.now())
                .build();

        Consultation savedConsultation = consultationRepository.save(consultation);
        log.info("Consultation created with id: {} for pet: {}", savedConsultation.getId(), pet.getId());
        return convertToDto(savedConsultation);
    }

    @Transactional(readOnly = true)
    public ConsultationDto getConsultationById(Long id) {
        Consultation consultation = consultationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Consultation not found"));
        return convertToDto(consultation);
    }

    @Transactional(readOnly = true)
    public List<ConsultationDto> getConsultationsByOwnerId(Long ownerId) {
        return consultationRepository.findByOwnerId(ownerId).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ConsultationDto> getConsultationsByVeterinarianId(Long veterinarianId) {
        return consultationRepository.findByVeterinarianId(veterinarianId).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ConsultationDto> getConsultationsByPetId(Long petId) {
        return consultationRepository.findByPetId(petId).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ConsultationDto> getAllConsultations() {
        return consultationRepository.findAll().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public ConsultationDto updateConsultation(Long id, ConsultationDto dto) {
        Consultation consultation = consultationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Consultation not found"));

        consultation.setTitle(dto.getTitle());
        consultation.setDescription(dto.getDescription());
        consultation.setSymptoms(dto.getSymptoms());
        consultation.setDiagnosis(dto.getDiagnosis());
        consultation.setNotes(dto.getNotes());
        consultation.setStatus(Consultation.ConsultationStatus.valueOf(dto.getStatus().toUpperCase()));

        if (dto.getVeterinarianId() != null) {
            User veterinarian = userRepository.findById(dto.getVeterinarianId())
                    .orElseThrow(() -> new RuntimeException("Veterinarian not found"));
            consultation.setVeterinarian(veterinarian);
        }

        if (Consultation.ConsultationStatus.COMPLETED.name().equals(dto.getStatus().toUpperCase())) {
            consultation.setCompletedAt(LocalDateTime.now());
        }

        Consultation updatedConsultation = consultationRepository.save(consultation);
        log.info("Consultation updated with id: {}", id);
        return convertToDto(updatedConsultation);
    }

    @Transactional
    public void deleteConsultation(Long id) {
        Consultation consultation = consultationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Consultation not found"));
        consultationRepository.delete(consultation);
        log.info("Consultation deleted with id: {}", id);
    }

    @Transactional
    public ConsultationDto assignVeterinarian(Long consultationId, Long veterinarianId) {
        Consultation consultation = consultationRepository.findById(consultationId)
                .orElseThrow(() -> new RuntimeException("Consultation not found"));

        User veterinarian = userRepository.findById(veterinarianId)
                .orElseThrow(() -> new RuntimeException("Veterinarian not found"));

        consultation.setVeterinarian(veterinarian);
        consultation.setStatus(Consultation.ConsultationStatus.SCHEDULED);

        Consultation updatedConsultation = consultationRepository.save(consultation);
        log.info("Veterinarian assigned to consultation: {}", consultationId);
        return convertToDto(updatedConsultation);
    }

    @Transactional
    public ConsultationDto rateConsultation(Long consultationId, Float rating, String feedback) {
        Consultation consultation = consultationRepository.findById(consultationId)
                .orElseThrow(() -> new RuntimeException("Consultation not found"));

        consultation.setRating(rating);
        consultation.setFeedback(feedback);

        Consultation updatedConsultation = consultationRepository.save(consultation);
        log.info("Consultation rated with id: {}", consultationId);
        return convertToDto(updatedConsultation);
    }

    private ConsultationDto convertToDto(Consultation consultation) {
        return ConsultationDto.builder()
                .id(consultation.getId())
                .petId(consultation.getPet().getId())
                .ownerId(consultation.getOwner().getId())
                .veterinarianId(consultation.getVeterinarian() != null ? consultation.getVeterinarian().getId() : null)
                .title(consultation.getTitle())
                .description(consultation.getDescription())
                .symptoms(consultation.getSymptoms())
                .diagnosis(consultation.getDiagnosis())
                .notes(consultation.getNotes())
                .status(consultation.getStatus().name())
                .consultationDate(consultation.getConsultationDate())
                .completedAt(consultation.getCompletedAt())
                .rating(consultation.getRating())
                .feedback(consultation.getFeedback())
                .build();
    }
}
