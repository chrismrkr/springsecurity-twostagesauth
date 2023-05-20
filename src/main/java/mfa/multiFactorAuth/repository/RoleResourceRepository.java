package mfa.multiFactorAuth.repository;

import mfa.multiFactorAuth.domain.RoleResource;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository("roleResourceRepository")
public interface RoleResourceRepository extends JpaRepository<RoleResource, Long> {
}
