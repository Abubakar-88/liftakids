package org.liftakids.repositories;

import org.liftakids.entity.Donor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface DonorRepository extends JpaRepository<Donor, Long> {
    @Query("SELECT d FROM Donor d WHERE " +
            "LOWER(d.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(d.email) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "d.phone LIKE CONCAT('%', :searchTerm, '%')")
    List<Donor> searchDonors(@Param("searchTerm") String searchTerm);
    Optional<Donor> findByEmail(String email);
}
