package org.liftakids.repositories;

import org.liftakids.entity.address.Districts;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DistrictRepository extends JpaRepository<Districts, Long> {
}
