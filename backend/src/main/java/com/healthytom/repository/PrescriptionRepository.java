package com.healthytom.repository;

import com.healthytom.entity.Prescription;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PrescriptionRepository extends JpaRepository<Prescription, Long> {
    List<Prescription> findByConsultationId(Long consultationId);
    List<Prescription> findByPetId(Long petId);
    List<Prescription> findByVeterinarianId(Long veterinarianId);
}
