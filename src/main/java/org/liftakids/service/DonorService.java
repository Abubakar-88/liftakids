package org.liftakids.service;

import org.liftakids.dto.donor.DonorRequestDto;
import org.liftakids.dto.donor.DonorResponseDto;

import java.util.List;

public interface DonorService {
    DonorResponseDto createDonor(DonorRequestDto dto);
    List<DonorResponseDto> getAllDonors();
    DonorResponseDto getDonorById(Long id);
}
