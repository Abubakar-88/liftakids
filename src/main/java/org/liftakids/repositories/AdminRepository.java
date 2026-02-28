package org.liftakids.repositories;

import org.liftakids.entity.SystemAdmin;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface AdminRepository extends JpaRepository<SystemAdmin, Long> {
    Optional<SystemAdmin> findByUsername(String username);
    Optional<SystemAdmin> findByEmail(String email);
    List<SystemAdmin> findByActiveTrue();
    List<SystemAdmin> findByActiveFalse();
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);

    @Query("SELECT COUNT(a) FROM SystemAdmin a WHERE a.active = true")
    long countActiveAdmins();
}