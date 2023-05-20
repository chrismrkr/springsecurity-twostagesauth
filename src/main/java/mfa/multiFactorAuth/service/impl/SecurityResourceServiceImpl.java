package mfa.multiFactorAuth.service.impl;

import lombok.RequiredArgsConstructor;
import mfa.multiFactorAuth.domain.Resource;
import mfa.multiFactorAuth.domain.Role;
import mfa.multiFactorAuth.domain.RoleResource;
import mfa.multiFactorAuth.repository.ResourceRepository;
import mfa.multiFactorAuth.service.SecurityResourceService;
import org.springframework.security.access.ConfigAttribute;
import org.springframework.security.access.SecurityConfig;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

@Service("securityResourceService")
@RequiredArgsConstructor
public class SecurityResourceServiceImpl implements SecurityResourceService {
    private final ResourceRepository resourceRepository;
    @Override
    @Transactional
    public LinkedHashMap<RequestMatcher, List<ConfigAttribute>> getResourceList() {
        /**
         * key: 리소스
         * value: 접근 가능한 권한 리스트
         */
        LinkedHashMap<RequestMatcher, List<ConfigAttribute>> result = new LinkedHashMap<>();
        List<Resource> resourceList = resourceRepository.findAll();
        resourceList.forEach(resource -> {
            List<ConfigAttribute> configAttributes = new ArrayList<>();
            for(RoleResource roleResource : resource.getRoleResourceSet()) {
                Role role = roleResource.getRole();
                configAttributes.add(new SecurityConfig(role.getRoleName()));
            }
            result.put(new AntPathRequestMatcher(resource.getResourceName()), configAttributes);
        });
        return result;
    }
}
