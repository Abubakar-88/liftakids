package org.liftakids.repositories;

import org.liftakids.entity.InstitutionType;
import org.liftakids.entity.Institutions;
import org.liftakids.entity.Sponsorship;
import org.liftakids.entity.SponsorshipStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface InstitutionRepository extends JpaRepository<Institutions, Long> {
    Optional<Institutions> findByEmail(String email);
    @Query("SELECT i FROM Institutions i WHERE i.unionOrArea.unionOrAreaId = :unionOrAreaId")
    List<Institutions> findByUnionOrAreaId(@Param("unionOrAreaId") Long unionOrAreaId);

    @EntityGraph(attributePaths = {"division", "district", "thana", "unionOrArea"})
    @Query("SELECT i FROM Institutions i")
    Page<Institutions> findAllWithLocations(Pageable pageable);
    // For paginated results (no filters)
//    Page<Institutions> findAllByOrderByName(Pageable pageable);
//    @Query("SELECT i FROM Institutions i " +
//            "LEFT JOIN FETCH i.division" +
//            "LEFT JOIN FETCH i.district " +
//            "LEFT JOIN FETCH i.thana " +
//            "LEFT JOIN FETCH i.unionOrArea")
//    Page<Institutions> findAllWithLocations(Pageable pageable);


    boolean existsByEmail(String email);
    List<Institutions> findByApprovedTrue();

    List<Institutions> findByType(InstitutionType type);
    Optional<Institutions> findByInstitutionName(String name);


}
