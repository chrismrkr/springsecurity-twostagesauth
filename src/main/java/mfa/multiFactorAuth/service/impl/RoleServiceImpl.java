package mfa.multiFactorAuth.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mfa.multiFactorAuth.domain.Role;
import mfa.multiFactorAuth.repository.RoleRepository;
import mfa.multiFactorAuth.service.RoleService;
import org.springframework.stereotype.Service;
import javax.annotation.PostConstruct;
import javax.transaction.Transactional;

@Service("roleService")
@RequiredArgsConstructor
@Slf4j
public class RoleServiceImpl implements RoleService {
    private final RoleRepository roleRepository;
    @PostConstruct
    @Transactional
    void saveInitRole() {
        Role role = Role.builder().roleName("ROLE_USER")
                .roleDescription("USER")
                .build();
        roleRepository.save(role);
        log.info("init roleService");
    }
}
