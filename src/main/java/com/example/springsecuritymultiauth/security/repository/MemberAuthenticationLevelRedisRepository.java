package com.example.springsecuritymultiauth.security.repository;

import com.example.springsecuritymultiauth.Member.domain.Member;
import com.example.springsecuritymultiauth.Member.entity.MemberEntity;
import com.example.springsecuritymultiauth.security.manager.port.MemberAuthenticationLevelRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;

import javax.swing.text.html.Option;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
public class MemberAuthenticationLevelRedisRepository implements MemberAuthenticationLevelRepository {
    private final RedisTemplate redisTemplate;
    private static final int INIT_AUTHENTICATION_LEVEL = 1;
    private static final long TIME_OUT = 30L;
    private static final TimeUnit MINUTES = TimeUnit.MINUTES;
    @Override
    public Optional<Integer> findCurrentAuthenticationLevel(String memberUsername) {
        ValueOperations<String, Object> valueOperations = redisTemplate.opsForValue();
        Object ret = valueOperations.get(memberUsername);
        if(ret == null) {
            return Optional.empty();
        }
        Integer currentAuthenticationLevel =  Integer.parseInt((String)ret);
        return Optional.of(currentAuthenticationLevel);
    }

    @Override
    public boolean initailizeAuthenticationLevel(String memberUsername) {
        boolean ret = updateAuthenticationLevel(memberUsername, INIT_AUTHENTICATION_LEVEL);
        return ret;
    }

    @Override
    public boolean updateAuthenticationLevel(String memberUsername, Integer level) {
        ValueOperations<String, Object> valueOperations = redisTemplate.opsForValue();
        try {
            valueOperations.set(memberUsername, level.toString(), TIME_OUT, MINUTES);
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    @Override
    public boolean deleteAuthenticationLevel(String memberUsername) {
        ValueOperations<String, Object> valueOperations = redisTemplate.opsForValue();
        try {
            Object andDelete = valueOperations.getAndDelete(memberUsername);
        } catch (Exception e) {
            return false;
        }
        return true;
    }
}
