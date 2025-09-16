package org.liftakids.service.impl;

import lombok.RequiredArgsConstructor;
import org.liftakids.dto.donor.DonorRequestDto;
import org.liftakids.dto.donor.DonorResponseDto;
import org.liftakids.dto.donor.LoginRequestDto;
import org.liftakids.dto.donor.LoginResponseDto;
import org.liftakids.entity.Donor;
import org.liftakids.exception.BusinessException;
import org.liftakids.exception.ResourceNotFoundException;
import org.liftakids.repositories.DonorRepository;
import org.liftakids.service.DonorService;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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

    public Page<DonorResponseDto> getAllDonorsWithPagination(int page, int size, String sortBy, String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase(Sort.Direction.ASC.name())
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();

        Pageable pageable = PageRequest.of(page, size, sort);
        return donorRepository.findAll(pageable)
                .map(this::convertToDto);
    }

    private DonorResponseDto convertToDto(Donor donor) {
        return DonorResponseDto.fromEntity(donor);
    }

    public List<DonorResponseDto> searchDonors(String searchTerm) {
        return donorRepository.searchDonors(searchTerm).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<DonorResponseDto> getAllDonors() {
        return donorRepository.findAll().stream()
                .map(donor -> modelMapper.map(donor, DonorResponseDto.class))
                .collect(Collectors.toList());
    }

    @Override
    public DonorResponseDto getDonorById(Long donarId) {
        Donor donor = donorRepository.findById(donarId)
                .orElseThrow(() -> new RuntimeException("Donor not found with id: " + donarId));
        return modelMapper.map(donor, DonorResponseDto.class);
    }
    public void deleteDonor(Long donarId) {
        Donor donor = donorRepository.findById(donarId)
                .orElseThrow(() -> new ResourceNotFoundException("Donor not found with id: " + donarId));

        if (!donor.getActiveSponsorships().isEmpty()) {
            throw new BusinessException("Cannot delete donor with active sponsorships");
        }

        donorRepository.delete(donor);
    }

    @Override
    public LoginResponseDto loginDonor(LoginRequestDto loginRequest) {
         //find the donor
        Donor donor = donorRepository.findByEmail(loginRequest.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("Donor not found with email: " + loginRequest.getEmail()));
        if (!loginRequest.getPassword().equals(donor.getPassword())) {
            throw new BusinessException("Invalid password");
        }
        // Check if donor is active
        if (!donor.isStatus()) {
            throw new BusinessException("Donor account is deactivated");
        }

        // Check if donor is active
        if (!donor.isStatus()) {
            throw new BusinessException("Donor account is deactivated");
        }

        // Login successful, response prepare করুন
        LoginResponseDto response = new LoginResponseDto();
        response.setSuccess(true);
        response.setMessage("Login successful");
        response.setDonor(convertToDto(donor));

        return response;
    }

}
