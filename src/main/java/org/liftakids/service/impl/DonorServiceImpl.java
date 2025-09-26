package org.liftakids.service.impl;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.liftakids.dto.donor.*;
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
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DonorServiceImpl implements DonorService {
   // private final PasswordEncoder passwordEncoder;
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
    public DonorResponseDto updateDonor(Long donorId, DonorUpdateRequestDto updateRequestDto) {
        Donor donor = donorRepository.findById(donorId)
                .orElseThrow(() -> new EntityNotFoundException("Donor not found with id: " + donorId));

        // Check if email is already taken by another donor
        if (updateRequestDto.getEmail() != null &&
                !updateRequestDto.getEmail().equals(donor.getEmail())) {
            Optional<Donor> existingDonor = donorRepository.findByEmail(updateRequestDto.getEmail());
            if (existingDonor.isPresent() && !existingDonor.get().getDonorId().equals(donorId)) {
                throw new IllegalArgumentException("Email already taken");
            }
        }

        // Update fields if they are provided
        if (updateRequestDto.getName() != null) {
            donor.setName(updateRequestDto.getName());
        }
        if (updateRequestDto.getEmail() != null) {
            donor.setEmail(updateRequestDto.getEmail());
        }
        if (updateRequestDto.getPhone() != null) {
            donor.setPhone(updateRequestDto.getPhone());
        }
        if (updateRequestDto.getAddress() != null) {
            donor.setAddress(updateRequestDto.getAddress());
        }
        if (updateRequestDto.getType() != null) {
            donor.setType(updateRequestDto.getType());
        }

        Donor updatedDonor = donorRepository.save(donor);
        return convertToDto(updatedDonor);
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
    @Override
    public PasswordResetResponseDto changePassword(Long donorId, PasswordChangeRequestDto request) {
        try {
            // Validate passwords match
            if (!request.getNewPassword().equals(request.getConfirmPassword())) {
                return new PasswordResetResponseDto("New password and confirm password do not match", false);
            }

            // Find donor
            Donor donor = donorRepository.findById(donorId)
                    .orElseThrow(() -> new RuntimeException("Donor not found"));

            // Verify current password
//            if (!passwordEncoder.matches(request.getCurrentPassword(), donor.getPassword())) {
//                return new PasswordResetResponseDto("Current password is incorrect", false);
//            }
            if (!request.getCurrentPassword().equals(donor.getPassword())) {
                return new PasswordResetResponseDto("Current password is incorrect", false);
            }
            // Validate new password strength (optional)
            if (request.getNewPassword().length() < 6) {
                return new PasswordResetResponseDto("New password must be at least 6 characters long", false);
            }

            // Update password
           // donor.setPassword(passwordEncoder.encode(request.getNewPassword()));
            donor.setPassword(request.getNewPassword());
            donorRepository.save(donor);

            return new PasswordResetResponseDto("Password changed successfully", true);

        } catch (Exception e) {
            return new PasswordResetResponseDto("Error changing password: " + e.getMessage(), false);
        }
    }
}
