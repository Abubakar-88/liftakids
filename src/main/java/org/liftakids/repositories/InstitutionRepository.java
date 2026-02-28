package org.liftakids.repositories;

import org.liftakids.entity.InstitutionType;
import org.liftakids.entity.Institutions;
import org.liftakids.entity.enm.InstitutionStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Map;
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

    List<Institutions> findByStatus(InstitutionStatus status);
    boolean existsByEmail(String email);
    List<Institutions> findByApprovedByTrue();

    List<Institutions> findByType(InstitutionType type);
    Optional<Institutions> findByInstitutionName(String name);

    List<Institutions> findAll();

    // Optional: Find by status
    List<Institutions> findByStatus(String status);

    // Count by approval status
    Long countByIsApproved(Boolean isApproved);

    // Count by status enum
    Long countByStatus(InstitutionStatus status);

    // Count all institutions
    //Long countAll();

    // Custom query for status statistics
    @Query("SELECT new map(i.status as status, COUNT(i) as count) " +
            "FROM Institutions i " +
            "GROUP BY i.status")
    List<Map<String, Object>> countByStatusGroup();

    // Or using native query
    @Query(value = "SELECT status, COUNT(*) as count FROM institutions GROUP BY status",
            nativeQuery = true)
    List<Object[]> getStatusStatisticsNative();

}
