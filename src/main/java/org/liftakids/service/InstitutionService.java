package org.liftakids.service;

import org.liftakids.dto.institute.InstitutionRequestDto;
import org.liftakids.dto.institute.InstitutionResponseDto;

import java.util.List;

public interface InstitutionService {
    InstitutionResponseDto createInstitution(InstitutionRequestDto requestDto);
    List<InstitutionResponseDto> getAllInstitutions();
    InstitutionResponseDto getInstitutionById(Long id);
    InstitutionResponseDto updateInstitution(Long id, InstitutionRequestDto requestDto);

    void deleteInstitution(Long id);

    List<InstitutionResponseDto> getApprovedInstitutions();

    List<InstitutionResponseDto> getInstitutionsByType(String type);
    InstitutionResponseDto getByIdOrName(String value);
}
