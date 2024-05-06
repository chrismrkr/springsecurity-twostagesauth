package com.example.springsecuritymultiauth.Member.repository;

import com.example.springsecuritymultiauth.Member.entity.MemberEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import javax.swing.text.html.Option;
import java.util.Optional;

public interface MemberJpaRepository extends JpaRepository<MemberEntity, Long> {
    Optional<MemberEntity> findByUsername(String username);
}
