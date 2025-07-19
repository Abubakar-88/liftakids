package org.liftakids.repositories;

import org.liftakids.entity.InstitutionType;
import org.liftakids.entity.Institutions;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface InstitutionRepository extends JpaRepository<Institutions, Long> {
    boolean existsByEmail(String email);
    List<Institutions> findByApprovedTrue();

    List<Institutions> findByType(InstitutionType type);
    Optional<Institutions> findByInstitutionName(String name);
}
