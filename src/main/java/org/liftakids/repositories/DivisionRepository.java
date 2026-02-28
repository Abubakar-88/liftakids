package org.liftakids.repositories;

import org.liftakids.entity.address.Divisions;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface DivisionRepository extends JpaRepository<Divisions,Long> {
    @Query("SELECT d FROM Divisions d ORDER BY d.divisionName")
    List<Divisions> findAllBasic();
}
