package org.liftakids.service;

import org.liftakids.dto.divison.DivisionDto;
import org.liftakids.dto.divison.DivisionResponseDTO;

import java.util.List;

public interface DivisionService {
    DivisionDto create(DivisionDto dto);
    List<DivisionResponseDTO> getAll();
    List<DivisionDto> createAll(List<DivisionDto> divisionDtos);
    DivisionResponseDTO updateDivision(Long divisionId, DivisionDto divisionRequestDTO);
}
