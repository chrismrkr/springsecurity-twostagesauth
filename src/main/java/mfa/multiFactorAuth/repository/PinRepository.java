package mfa.multiFactorAuth.repository;

import mfa.multiFactorAuth.domain.Pin;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PinRepository extends JpaRepository<Pin, String> {
}
