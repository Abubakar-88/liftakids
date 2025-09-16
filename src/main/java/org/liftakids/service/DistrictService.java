package org.liftakids.service;

import org.liftakids.dto.district.DistrictDto;
import org.liftakids.dto.district.DistrictResponseDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface DistrictService {
    DistrictDto create(DistrictDto dto);
    List<DistrictDto> getAll();
    List<DistrictDto> createMultiple(List<DistrictDto> dtoList);
    List<DistrictResponseDTO> getDistrictsByDivisionId(Long divisionId);
    Page<DistrictResponseDTO> getAllDistricts(Pageable pageable);

}
