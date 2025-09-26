package org.liftakids.service;

import org.liftakids.dto.donor.*;
import org.springframework.data.domain.Page;

import java.util.List;

public interface DonorService {
    DonorResponseDto createDonor(DonorRequestDto dto);
    List<DonorResponseDto> getAllDonors();
    DonorResponseDto getDonorById(Long donarId);
    List<DonorResponseDto> searchDonors(String searchTerm);
    Page<DonorResponseDto> getAllDonorsWithPagination(int page, int size, String sortBy, String sortDir);
    void deleteDonor(Long donarId);
    LoginResponseDto loginDonor(LoginRequestDto loginRequest);
    DonorResponseDto updateDonor(Long id, DonorUpdateRequestDto updateRequestDto);
    PasswordResetResponseDto changePassword(Long donorId, PasswordChangeRequestDto request);
}
