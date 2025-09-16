package org.liftakids.service;

import org.liftakids.dto.institute.*;
import org.liftakids.dto.student.StudentResponseDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface InstitutionService {
    InstitutionResponseDto createInstitution(InstitutionRequestDto requestDto);
    LoginResponseDto login(LoginRequestDto loginRequest);
    // Filtered (no pagination)
    List<InstitutionBasicResponse> getByUnionOrArea(Long unionOrAreaId);
    Page<InstitutionBasicResponse> getAllInstitutions(Pageable pageable);
    InstitutionResponseDto getInstitutionById(Long id);
    List<InstitutionResponseDto> getAllInstitutionsList();
    InstitutionResponseDto updateInstitution(Long id, UpdateInstitutionDto requestDto);

    void deleteInstitution(Long id);
    Page<StudentResponseDto> getAllStudentsWithSponsorsByInstitution(Long institutionId, Pageable pageable);
    List<InstitutionResponseDto> getApprovedInstitutions();

    List<InstitutionResponseDto> getInstitutionsByType(String type);
    InstitutionResponseDto getByIdOrName(String value);
    String getInstitutionNameById(Long id);
   // Page<StudentResponseDto> getAllStudentsWithSponsorsByInstitution(Long institutionId, Pageable pageable);
}
