package org.liftakids.service;

import org.liftakids.dto.district.DistrictDto;

import java.util.List;

public interface DistrictService {
    DistrictDto create(DistrictDto dto);
    List<DistrictDto> getAll();
    List<DistrictDto> createMultiple(List<DistrictDto> dtoList);


}
