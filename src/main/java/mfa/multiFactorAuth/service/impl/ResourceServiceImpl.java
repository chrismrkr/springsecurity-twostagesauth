package mfa.multiFactorAuth.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mfa.multiFactorAuth.domain.Resource;
import mfa.multiFactorAuth.domain.Role;
import mfa.multiFactorAuth.domain.RoleResource;
import mfa.multiFactorAuth.repository.ResourceRepository;
import mfa.multiFactorAuth.repository.RoleRepository;
import mfa.multiFactorAuth.repository.RoleResourceRepository;
import mfa.multiFactorAuth.service.ResourceService;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.transaction.Transactional;

@Slf4j
@Service("resourceService")
@DependsOn("roleService")
@RequiredArgsConstructor
public class ResourceServiceImpl implements ResourceService {
    private final ResourceRepository resourceRepository;
    private final RoleResourceRepository roleResourceRepository;
    private final RoleRepository roleRepository;

    @Override
    @Transactional
    public void createResource(Resource resource) {
        resourceRepository.save(resource);
    }

    @PostConstruct
    @Transactional
    void saveInitResource() {
        Resource resource = Resource.builder().resourceName("/**").orderNum(1).build();
        createResource(resource);
        Role roleUser = roleRepository.findByRoleName("ROLE_USER");
        RoleResource roleResource = RoleResource.builder().resource(resource).role(roleUser).build();
        roleResourceRepository.save(roleResource);
    }
}
