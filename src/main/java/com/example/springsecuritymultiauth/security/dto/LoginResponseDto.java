package com.example.springsecuritymultiauth.security.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class LoginResponseDto {
    private String status;
    private Integer completeAuthenticationLevel;
    private Integer nextAuthenticationLevel;
}
