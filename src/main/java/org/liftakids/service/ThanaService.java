package org.liftakids.service;

import org.liftakids.dto.thana.ThanaDto;
import org.liftakids.dto.thana.ThanaResponseDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ThanaService {
    ThanaDto create(ThanaDto dto);
    List<ThanaResponseDTO> getAll();
    Page<ThanaResponseDTO> getAllThanas(Pageable pageable);
    List<ThanaResponseDTO> getThanasByDistrictId(Long districtId);
    ThanaResponseDTO updateThana(Long thanaId, ThanaDto ThanaDto);
}
