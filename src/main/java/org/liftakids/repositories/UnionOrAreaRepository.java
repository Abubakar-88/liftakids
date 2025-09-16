package org.liftakids.repositories;

import org.liftakids.entity.address.UnionOrArea;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UnionOrAreaRepository extends JpaRepository<UnionOrArea, Long> {
    @EntityGraph(attributePaths = {"thana", "thana.district", "thana.district.division"})
    Page<UnionOrArea> findByThana_ThanaId(Long thanaId, Pageable pageable);

    @EntityGraph(attributePaths = {"thana", "thana.district", "thana.district.division"})
    List<UnionOrArea> findByThana_ThanaId(Long thanaId);
}
