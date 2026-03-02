package com.healthytom.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuthenticationResponse {
    @JsonProperty("token")
    private String accessToken;
    
    @JsonProperty("refreshToken")
    private String refreshToken;
    
    private UserDto user;
}
