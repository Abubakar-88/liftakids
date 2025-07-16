package org.liftakids.service.impl;

import lombok.RequiredArgsConstructor;
import org.liftakids.dto.donor.DonorRequestDto;
import org.liftakids.dto.donor.DonorResponseDto;
import org.liftakids.entity.Donor;
import org.liftakids.repositories.DonorRepository;
import org.liftakids.service.DonorService;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DonorServiceImpl implements DonorService {

    private final DonorRepository donorRepository;
    private final ModelMapper modelMapper;

    @Override
    public DonorResponseDto createDonor(DonorRequestDto dto) {
        Donor donor = modelMapper.map(dto, Donor.class);
        donor.setDonorId(null); // ensure it's treated as new
        return modelMapper.map(donorRepository.save(donor), DonorResponseDto.class);
    }

    @Override
    public List<DonorResponseDto> getAllDonors() {
        return donorRepository.findAll().stream()
                .map(donor -> modelMapper.map(donor, DonorResponseDto.class))
                .collect(Collectors.toList());
    }

    @Override
    public DonorResponseDto getDonorById(Long id) {
        Donor donor = donorRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Donor not found with id: " + id));
        return modelMapper.map(donor, DonorResponseDto.class);
    }
}
