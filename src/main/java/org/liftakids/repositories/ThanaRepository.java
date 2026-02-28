package org.liftakids.repositories;

import org.liftakids.entity.address.Districts;
import org.liftakids.entity.address.Thanas;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ThanaRepository extends JpaRepository<Thanas,Long> {

    @Query("SELECT t FROM Thanas t WHERE t.district.districtId = :districtId")
    List<Thanas> findByDistrictId(Long districtId);
    // OR using JOIN FETCH
    @Query("SELECT t FROM Thanas t JOIN FETCH t.district d JOIN FETCH d.division WHERE t.thanaId = :thana")
    Optional<Thanas> findWithDivisionById(@Param("thanaId") Long thanaId);


    @Query("SELECT d FROM Districts d WHERE d.division.divisionId = :divisionId")
    List<Districts> findByDivisionId(@Param("divisionId") Long divisionId);

    boolean existsByThanaNameAndDistrictDistrictId(String thanaName, Long districtId);

    @Query("SELECT t FROM Thanas t WHERE t.district.districtId IN :districtIds")
    List<Thanas> findByDistrictIdIn(@Param("districtIds") List<Long> districtIds);

    @Query("SELECT DISTINCT t FROM Thanas t " +
            "LEFT JOIN FETCH t.district d " +
            "LEFT JOIN FETCH d.division " +
            "WHERE t.district.districtId = :districtId")
    List<Thanas> findByDistrictIdWithEagerFetch(@Param("districtId") Long districtId);

}
