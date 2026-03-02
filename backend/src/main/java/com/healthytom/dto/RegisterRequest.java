package com.healthytom.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RegisterRequest {
    private String email;
    private String password;
    private String firstName;
    private String lastName;
    private String phoneNumber;
    private String role; // OWNER, VETERINARIAN
    private String specialization;
    private String licenseNumber;
}
