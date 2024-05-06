package com.example.springsecuritymultiauth.Member.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity(name = "MEMBER")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MemberEntity {
    @Id
    @Column(name = "member_id")
    @GeneratedValue
    private Long id;
    private String username;
    private String password;
    private int requiredAuthenticationLevel;

    @Builder
    private MemberEntity(Long id, String username, String password, int requiredAuthenticationLevel) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.requiredAuthenticationLevel = requiredAuthenticationLevel;
    }
}
