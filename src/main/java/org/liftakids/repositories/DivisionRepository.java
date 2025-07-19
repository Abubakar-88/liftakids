package org.liftakids.repositories;

import org.liftakids.entity.address.Divisions;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DivisionRepository extends JpaRepository<Divisions,Long> {
}
