package com.healthytom.dto;

import lombok.*;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConsultationDto {
    private Long id;
    private Long petId;
    private Long ownerId;
    private Long veterinarianId;
    private String title;
    private String description;
    private String symptoms;
    private String diagnosis;
    private String notes;
    private String status;
    private LocalDateTime consultationDate;
    private LocalDateTime completedAt;
    private Float rating;
    private String feedback;
}
