package org.liftakids.repositories;

import org.liftakids.entity.address.Districts;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface DistrictRepository extends JpaRepository<Districts, Long> {
//    List<Districts> findByDivisionDivisionId(Long divisionId);
        @EntityGraph(attributePaths = {"division"})
        Optional<Districts> findWithDivisionByDistrictId(Long districtId);

    @Query("SELECT d FROM Districts d WHERE d.division.divisionId = :divisionId")
    List<Districts> findByDivisionId(@Param("divisionId") Long divisionId);

//    @Query("SELECT d FROM Districts d WHERE d.division.divisionId IN :divisionIds")
//    List<Districts> findByDivisionIds(List<Long> divisionIds);
@Query("SELECT DISTINCT d FROM Districts d " +
        "LEFT JOIN FETCH d.division " +
        "LEFT JOIN FETCH d.thanas t " +
        "LEFT JOIN FETCH t.unionOrAreas")
Page<Districts> findAllWithFetch(Pageable pageable);
}
