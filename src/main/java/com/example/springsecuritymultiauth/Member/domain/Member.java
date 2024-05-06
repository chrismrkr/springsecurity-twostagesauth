package com.example.springsecuritymultiauth.Member.domain;

import com.example.springsecuritymultiauth.Member.entity.MemberEntity;
import lombok.Builder;
import lombok.Getter;

@Getter
public class Member {
    private Long id;
    private String username;
    private String password;
    private int requiredAuthenticationLevel;

    @Builder
    private Member(Long id, String username, String password, int requiredAuthenticationLevel) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.requiredAuthenticationLevel = requiredAuthenticationLevel;
    }

    public MemberEntity toEntity() {
        MemberEntity memberEntity = MemberEntity.builder()
                .id(this.id)
                .username(this.username)
                .password(this.password)
                .requiredAuthenticationLevel(this.requiredAuthenticationLevel)
                .build();
        return memberEntity;
    }
    static public Member from(MemberEntity memberEntity) {
        Member member = Member.builder()
                .id(memberEntity.getId())
                .username(memberEntity.getUsername())
                .password(memberEntity.getPassword())
                .requiredAuthenticationLevel(memberEntity.getRequiredAuthenticationLevel())
                .build();
        return member;
    }
}
