package org.liftakids.service;

import org.liftakids.dto.sponsorship.SponsorshipRequestDto;
import org.liftakids.dto.sponsorship.SponsorshipResponseDto;
import org.liftakids.dto.sponsorship.SponsorshipSearchRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Map;

public interface SponsorshipService {
    SponsorshipResponseDto createSponsorship(SponsorshipRequestDto request);
    List<SponsorshipResponseDto> getOverdueSponsorships();
    Page<SponsorshipResponseDto> searchSponsorships(SponsorshipSearchRequest request, Pageable pageable);
    List<SponsorshipResponseDto> getByStudentId(Long studentId);
    Page<SponsorshipResponseDto> getAllSponsorships(Pageable pageable);
    SponsorshipResponseDto getSponsorshipById(Long id);
    List<SponsorshipResponseDto> getByDonorId(Long donorId);
    SponsorshipResponseDto cancelSponsorship(Long id);
    Page<SponsorshipResponseDto> getSponsorshipsByDonorId(Long donorId, Pageable pageable);
    List<SponsorshipResponseDto> getSponsorshipsByDonorId(Long donorId);
    List<SponsorshipResponseDto> getPendingSponsorshipsForInstitution(Long institutionId);
    List<SponsorshipResponseDto> getPendingPaymentSponsorshipsOptimized(Long institutionId);
    Map<String, Long> getSponsorshipStatusCounts(Long institutionId);

}
