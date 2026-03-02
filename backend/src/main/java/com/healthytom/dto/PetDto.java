package com.healthytom.dto;

import lombok.*;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PetDto {
    private Long id;
    private String name;
    private String species;
    private String breed;
    private String color;
    private Double weight;
    private LocalDate dateOfBirth;
    private String microchipNumber;
    private String medicalRecordNumber;
    private String medicalHistory;
    private String vaccinations;
    private String photoUrl;
    private Long ownerId;
}
