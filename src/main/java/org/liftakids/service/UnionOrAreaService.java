package org.liftakids.service;

import org.liftakids.dto.unionOrArea.UnionOrAreaDto;
import org.liftakids.dto.unionOrArea.UnionOrAreaResponseDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface UnionOrAreaService {
    UnionOrAreaResponseDTO create(UnionOrAreaDto dto);
    List<UnionOrAreaResponseDTO> getAll();
    Page<UnionOrAreaResponseDTO> getAllUnions(Pageable pageable);
    Page<UnionOrAreaResponseDTO> getUnionsByThanaId(Long thanaId, Pageable pageable);
    List<UnionOrAreaResponseDTO> getUnionsByThanaId(Long thanaId);
}
