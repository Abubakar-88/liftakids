package org.liftakids.service;

import org.liftakids.dto.donor.DonorRequestDto;
import org.liftakids.dto.donor.DonorResponseDto;
import org.liftakids.dto.donor.LoginRequestDto;
import org.liftakids.dto.donor.LoginResponseDto;
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
}
