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
    StatusStatisticsDto getStatusStatistics();
    InstitutionResponseDto getInstitutionById(Long id);
    List<InstitutionResponseDto> getAllInstitutionsList();
    InstitutionResponseDto updateInstitution(Long id, UpdateInstitutionDto requestDto);

    void deleteInstitution(Long id);
    Page<StudentResponseDto> getAllStudentsWithSponsorsByInstitution(Long institutionId, Pageable pageable);
    InstitutionResponseDto approveInstitution(Long institutionId, Long adminId, String approvalNotes);

    List<InstitutionResponseDto> getInstitutionsByType(String type);
    InstitutionResponseDto getByIdOrName(String value);
    String getInstitutionNameById(Long id);

    List<InstitutionResponseDto> getApprovedInstitutions();
    InstitutionResponseDto  suspendInstitution(Long institutionId, Long adminId, String suspensionReason);
    InstitutionResponseDto rejectInstitution(Long institutionId, Long adminId, String rejectionReason);
    InstitutionResponseDto  activateInstitution(Long institutionId, Long adminId);

    // OR if you need to return DTO

    //InstitutionResponseDto rejectInstitutionAndGet(Long institutionId, Long adminId, String rejectionReason);
   // Page<StudentResponseDto> getAllStudentsWithSponsorsByInstitution(Long institutionId, Pageable pageable);
}
