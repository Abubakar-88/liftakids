package org.liftakids.service;

import org.liftakids.dto.divison.DivisionDto;

import java.util.List;

public interface DivisionService {
    DivisionDto create(DivisionDto dto);
    List<DivisionDto> getAll();
    List<DivisionDto> createAll(List<DivisionDto> divisionDtos);

}
