package com.example.springsecuritymultiauth.Member.controller.dto.register;

import com.example.springsecuritymultiauth.Member.domain.Member;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;


@Data
@AllArgsConstructor
public class MemberRegisterResponseDto {
    private String status;
    private String username;
    private LocalDateTime createdTime;
}
