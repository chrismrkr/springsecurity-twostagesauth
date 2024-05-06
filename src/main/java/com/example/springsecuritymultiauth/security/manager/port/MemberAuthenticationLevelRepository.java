package com.example.springsecuritymultiauth.security.manager.port;

import com.example.springsecuritymultiauth.Member.domain.Member;

import java.util.Optional;

public interface MemberAuthenticationLevelRepository {
    Optional<Integer> findCurrentAuthenticationLevel(String memberUsername);
    boolean initailizeAuthenticationLevel(String memberUsername);
    boolean updateAuthenticationLevel(String memberUsername, Integer level);
    boolean deleteAuthenticationLevel(String memberUsername);
}
