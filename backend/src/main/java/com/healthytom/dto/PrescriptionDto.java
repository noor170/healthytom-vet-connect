package com.healthytom.dto;

import lombok.*;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PrescriptionDto {
    private Long id;
    private Long consultationId;
    private Long petId;
    private Long veterinarianId;
    private String medicationName;
    private String dosage;
    private String frequency;
    private Integer duration;
    private String instructions;
    private String sideEffects;
    private String status;
    private LocalDate prescribedDate;
    private LocalDate startDate;
    private LocalDate endDate;
    private String notes;
}
