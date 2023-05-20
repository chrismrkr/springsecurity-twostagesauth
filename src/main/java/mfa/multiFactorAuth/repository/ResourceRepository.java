package mfa.multiFactorAuth.repository;

import mfa.multiFactorAuth.domain.Resource;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository("resourceRepository")
public interface ResourceRepository extends JpaRepository<Resource, Long> {
}
