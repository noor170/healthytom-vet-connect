package com.healthytom.repository;

import com.healthytom.entity.Consultation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ConsultationRepository extends JpaRepository<Consultation, Long> {
    List<Consultation> findByOwnerId(Long ownerId);
    List<Consultation> findByVeterinarianId(Long veterinarianId);
    List<Consultation> findByPetId(Long petId);
}
