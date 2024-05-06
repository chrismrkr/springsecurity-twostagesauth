package com.example.springsecuritymultiauth.Member.controller.dto.register;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class MemberRegisterRequestDto {
    private String username;
    private String password;
}
