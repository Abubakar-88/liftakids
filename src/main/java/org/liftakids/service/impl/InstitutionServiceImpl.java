package org.liftakids.service.impl;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.liftakids.dto.district.DistrictResponseDTO;
import org.liftakids.dto.divison.DivisionResponseDTO;
import org.liftakids.dto.institute.*;
import org.liftakids.dto.student.StudentResponseDto;
import org.liftakids.dto.thana.ThanaResponseDTO;
import org.liftakids.dto.unionOrArea.UnionOrAreaResponseDTO;
import org.liftakids.entity.*;
import org.liftakids.entity.address.Districts;
import org.liftakids.entity.address.Divisions;
import org.liftakids.entity.address.Thanas;
import org.liftakids.entity.address.UnionOrArea;

import org.liftakids.exception.ResourceNotFoundException;
import org.liftakids.repositories.*;
import org.liftakids.service.InstitutionService;
import org.modelmapper.ModelMapper;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class InstitutionServiceImpl implements InstitutionService {

    private final UnionOrAreaRepository unionOrAreaRepository;
    private final StudentRepository studentRepository;
    private final DivisionRepository divisionRepository;
    private final DistrictRepository districtRepository;
    private final ThanaRepository thanaRepository;
    private final SponsorshipRepository sponsorshipRepository;

    private final InstitutionRepository institutionRepository;
    private final ModelMapper modelMapper;

    private static final Logger log = Logger.getLogger(InstitutionServiceImpl.class.getName());

    @Override
    public InstitutionResponseDto createInstitution(InstitutionRequestDto requestDto) {
        // Fetch all location entities
        Divisions division = divisionRepository.findById(requestDto.getDivisionId())
                .orElseThrow(() -> new ResourceNotFoundException("Division not found"));

        Districts district = districtRepository.findById(requestDto.getDistrictId())
                .orElseThrow(() -> new ResourceNotFoundException("District not found"));

        Thanas thana = thanaRepository.findById(requestDto.getThanaId())
                .orElseThrow(() -> new ResourceNotFoundException("Thana not found"));

        UnionOrArea unionOrArea = unionOrAreaRepository.findById(requestDto.getUnionOrAreaId())
                .orElseThrow(() -> new ResourceNotFoundException("Union/Area not found"));

        // Map and set all properties
        Institutions institution = modelMapper.map(requestDto, Institutions.class);

        institution.setDivision(division);
        institution.setDistrict(district);
        institution.setThana(thana);
        institution.setUnionOrArea(unionOrArea);
        institution.setRegistrationDate(LocalDateTime.now());
        institution.setUpdateDate(LocalDateTime.now());

        // Save institution
        Institutions saved = institutionRepository.save(institution);

        return convertToDto(saved);
    }


    private InstitutionResponseDto convertToDto(Institutions institution) {
        InstitutionResponseDto dto = modelMapper.map(institution, InstitutionResponseDto.class);


        UnionOrArea unionOrArea = institution.getUnionOrArea();
        Thanas thana = unionOrArea.getThana();
        Districts district = thana.getDistrict();
        Divisions division = district.getDivision();

        dto.setUnionOrAreaId(unionOrArea.getUnionOrAreaId());
        dto.setThanaId(thana.getThanaId());
        dto.setDistrictId(district.getDistrictId());
        dto.setDivisionId(division.getDivisionId());

        return dto;
    }
    public LoginResponseDto login(LoginRequestDto loginRequest) {
        Optional<Institutions> institutionOpt = institutionRepository.findByEmail(loginRequest.getEmail());

        if (institutionOpt.isEmpty()) {
            return new LoginResponseDto(false, "Institution not found with this email", null);
        }

        Institutions institution = institutionOpt.get();

        // Password matching (plain text comparison - পরে encryption add করবেন)
        if (!institution.getPassword().equals(loginRequest.getPassword())) {
            return new LoginResponseDto(false, "Invalid password", null);
        }

        InstitutionResponseDto institutionDto = convertToDto(institution);
        return new LoginResponseDto(true, "Login successful", institutionDto);
    }
    // Filtered (no pagination)
    @Override
    public List<InstitutionBasicResponse> getByUnionOrArea(Long unionOrAreaId) {
        return institutionRepository.findByUnionOrAreaId(unionOrAreaId).stream()
                .map(i -> modelMapper.map(i, InstitutionBasicResponse.class))
                .collect(Collectors.toList());
    }

    @Override
    public List<InstitutionResponseDto> getAllInstitutionsList() {
        return institutionRepository.findAll().stream()
                .map(institution -> modelMapper.map(institution, InstitutionResponseDto.class))
                .collect(Collectors.toList());
    }




    public Page<InstitutionBasicResponse> getAllInstitutions(Pageable pageable) {
        validateSortProperties(pageable.getSort());

        Page<Institutions> page = institutionRepository.findAllWithLocations(pageable);

        if (page.isEmpty()) {
            throw new EntityNotFoundException("No institutions found");
        }

        return page.map(this::convertToResponse);
    }

    private InstitutionBasicResponse convertToResponse(Institutions institution) {
        InstitutionBasicResponse response = modelMapper.map(institution, InstitutionBasicResponse.class);

        // Manually map the location objects if needed
        response.setDivision(modelMapper.map(institution.getDivision(), DivisionResponseDTO.class));
        response.setDistrict(modelMapper.map(institution.getDistrict(), DistrictResponseDTO.class));
        response.setThana(modelMapper.map(institution.getThana(), ThanaResponseDTO.class));
        response.setUnionOrArea(modelMapper.map(institution.getUnionOrArea(), UnionOrAreaResponseDTO.class));

        return response;
    }
    private void validateSortProperties(Sort sort) {
        for (Sort.Order order : sort) {
            if (!isValidSortProperty(order.getProperty())) {
                throw new IllegalArgumentException("Invalid sort property: " + order.getProperty());
            }
        }
    }

    private boolean isValidSortProperty(String property) {
        return Arrays.asList("institutionName", "type", "registrationDate")
                .contains(property);
    }


    @Override
    public InstitutionResponseDto getInstitutionById(Long id) {
        Institutions institution = institutionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Institution not found with id " + id));

        return modelMapper.map(institution, InstitutionResponseDto.class);
    }
    @Override
    public InstitutionResponseDto updateInstitution(Long id, UpdateInstitutionDto requestDto) {
        Institutions existing = institutionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Institution not found with id " + id));

        modelMapper.map(requestDto, existing);

        UnionOrArea unionOrArea = unionOrAreaRepository.findById(requestDto.getUnionOrAreaId())
                .orElseThrow(() -> new RuntimeException("UnionOrArea not found with id " + requestDto.getUnionOrAreaId()));
        existing.setUnionOrArea(unionOrArea);

        existing.setUpdateDate(LocalDateTime.now());

        Institutions updated = institutionRepository.save(existing);
        return modelMapper.map(updated, InstitutionResponseDto.class);
    }

    @Override
    public void deleteInstitution(Long id) {
        if (!institutionRepository.existsById(id)) {
            throw new RuntimeException("Institution not found with id " + id);
        }
        institutionRepository.deleteById(id);
    }

    @Override
    public List<InstitutionResponseDto> getApprovedInstitutions() {
        return institutionRepository.findByApprovedTrue()
                .stream()
                .map(institution -> modelMapper.map(institution, InstitutionResponseDto.class))
                .collect(Collectors.toList());
    }

    @Override
    public List<InstitutionResponseDto> getInstitutionsByType(String type) {
        InstitutionType institutionType;
        try {
            institutionType = InstitutionType.valueOf(type.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Invalid Institution type: " + type);
        }

        return institutionRepository.findByType(institutionType)
                .stream()
                .map(institution -> modelMapper.map(institution, InstitutionResponseDto.class))
                .collect(Collectors.toList());
    }

    @Override
    public InstitutionResponseDto getByIdOrName(String value) {
        Institutions institution;

        try {
            // Try parsing as ID (number)
            Long id = Long.parseLong(value);
            institution = institutionRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Institution not found with id " + id));
        } catch (NumberFormatException e) {
            // If not a number, treat as name
            institution = institutionRepository.findByInstitutionName(value)
                    .orElseThrow(() -> new RuntimeException("Institution not found with name: " + value));
        }
        return modelMapper.map(institution, InstitutionResponseDto.class);
    }

    @Override
    public String getInstitutionNameById(Long id) {
        Institutions institution = institutionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Institution not found with id " + id));
        return institution.getInstitutionName();
    }

//    @Override
//    public Page<StudentResponseDto> getAllStudentsWithSponsorsByInstitution(Long institutionId, Pageable pageable) {
//        return null;
//    }
  @Override
    public Page<StudentResponseDto> getAllStudentsWithSponsorsByInstitution(Long institutionId, Pageable pageable) {
        // Verify institution exists
        if (!institutionRepository.existsById(institutionId)) {
            throw new ResourceNotFoundException("Institution not found with id: " + institutionId);
        }

      Page<Student> sponsoredStudentsPage = studentRepository.findByInstitution_InstitutionsIdAndIsSponsored(institutionId, true, pageable);
      log.info("Found " + sponsoredStudentsPage.getTotalElements() + " sponsored students");
        // Convert to DTO with sponsors
        List<StudentResponseDto> studentDtos = sponsoredStudentsPage.getContent().stream()
                .map(this::convertToStudentResponseDtoWithSponsors)
                .collect(Collectors.toList());

        return new PageImpl<>(studentDtos, pageable, sponsoredStudentsPage.getTotalElements());
    }


    private StudentResponseDto convertToStudentResponseDtoWithSponsors(Student student) {
        StudentResponseDto dto = modelMapper.map(student, StudentResponseDto.class);

        // Set institution information
        dto.setInstitutionsId(student.getInstitution().getInstitutionsId());
        dto.setInstitutionName(student.getInstitution().getInstitutionName());

        // Get sponsorships only if student is sponsored
        if (student.isSponsored()) {
            List<Sponsorship> sponsorships = sponsorshipRepository.findByStudent_StudentId(student.getStudentId());

            BigDecimal totalSponsoredAmount = BigDecimal.ZERO;
            List<StudentResponseDto.SponsorInfoDto> sponsorInfoList = new ArrayList<>();

            for (Sponsorship sponsorship : sponsorships) {
                totalSponsoredAmount = totalSponsoredAmount.add(sponsorship.getTotalPaidAmount());

                StudentResponseDto.SponsorInfoDto sponsorInfo = StudentResponseDto.SponsorInfoDto.builder()
                        .donorId(sponsorship.getDonor().getDonorId())
                        .donorName(sponsorship.getDonor().getName())
                        .monthlyAmount(sponsorship.getMonthlyAmount())
                        .startDate(sponsorship.getStartDate())
                        .endDate(sponsorship.getEndDate())
                        .status(sponsorship.getStatus())
                        .lastPaymentDate(sponsorship.getLastPaymentDate())
                        .paidUpTo(sponsorship.getPaidUpTo())
                        .monthsPaid(sponsorship.getMonthsPaid())
                        .totalAmount(sponsorship.getTotalAmount())
                        .totalMonths(sponsorship.getTotalMonths())
                        .nextPaymentDueDate(sponsorship.getNextPaymentDueDate())
                        .build();

                sponsorInfoList.add(sponsorInfo);
            }

            dto.setSponsoredAmount(totalSponsoredAmount);
            dto.setSponsors(sponsorInfoList);
            dto.setFullySponsored(totalSponsoredAmount.compareTo(student.getRequiredMonthlySupport()) >= 0);
        } else {
            dto.setSponsoredAmount(BigDecimal.ZERO);
            dto.setSponsors(Collections.emptyList());
            dto.setFullySponsored(false);
        }

        dto.setSponsored(student.isSponsored());

        return dto;
    }


}
