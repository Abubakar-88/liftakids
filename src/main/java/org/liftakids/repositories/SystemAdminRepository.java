package org.liftakids.repositories;


import org.liftakids.entity.SystemAdmin;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SystemAdminRepository extends JpaRepository<SystemAdmin, Long> {
        Optional<SystemAdmin> findByUsername(String username);
        Optional<SystemAdmin> findByEmail(String email);
        Optional<SystemAdmin> findByAdminId(Long adminId);
        boolean existsByUsername(String username);
        boolean existsByEmail(String email);

}
