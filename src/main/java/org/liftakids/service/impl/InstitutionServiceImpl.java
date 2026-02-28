package org.liftakids.service.impl;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
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
import org.liftakids.entity.enm.InstitutionStatus;
import org.liftakids.entity.enm.NotificationType;
import org.liftakids.exception.DataTruncationException;
import org.liftakids.exception.ResourceNotFoundException;
import org.liftakids.repositories.*;
import org.liftakids.service.InstitutionService;
import org.liftakids.service.NotificationService;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
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
     private final AdminRepository adminRepository;
    private final InstitutionRepository institutionRepository;
    private final ModelMapper modelMapper;
    private final NotificationService notificationService;
    private final SystemAdminRepository systemAdminRepository;

    private static final Logger log = LoggerFactory.getLogger(InstitutionServiceImpl.class.getName());
    @Transactional
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
        institution.setStatus(InstitutionStatus.PENDING);
        // Save institution
        Institutions savedInstitution  = institutionRepository.save(institution);

        notificationService.sendInstitutionRegistrationNotification(savedInstitution );

        return convertToDto(savedInstitution );
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
    @Transactional
    @Override
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
    @Transactional
    @Override
    public List<InstitutionBasicResponse> getByUnionOrArea(Long unionOrAreaId) {
        return institutionRepository.findByUnionOrAreaId(unionOrAreaId)
                .stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<InstitutionResponseDto> getAllInstitutionsList() {
        return institutionRepository.findAll().stream()
                .map(institution -> modelMapper.map(institution, InstitutionResponseDto.class))
                .collect(Collectors.toList());
    }

   @Transactional
   @Override
    public Page<InstitutionBasicResponse> getAllInstitutions(Pageable pageable) {
        validateSortProperties(pageable.getSort());

        Page<Institutions> page = institutionRepository.findAllWithLocations(pageable);

        if (page.isEmpty()) {
            throw new EntityNotFoundException("No institutions found");
        }

        return page.map(this::convertToResponse);
    }
    private InstitutionBasicResponse convertToResponse(Institutions institution) {
        InstitutionBasicResponse response = new InstitutionBasicResponse();

        // Basic fields
        response.setInstitutionsId(institution.getInstitutionsId());
        response.setInstitutionName(institution.getInstitutionName());
        // Manually set the location IDs
        if (institution.getDivision() != null) {
            response.setDivisionId(institution.getDivision().getDivisionId());
        }

        if (institution.getDistrict() != null) {
            response.setDistrictId(institution.getDistrict().getDistrictId());
        }

        if (institution.getThana() != null) {
            response.setThanaId(institution.getThana().getThanaId());
        }

        if (institution.getUnionOrArea() != null) {
            response.setUnionOrAreaId(institution.getUnionOrArea().getUnionOrAreaId());
        }

        response.setType(institution.getType());
        response.setStatus(institution.getStatus());
        response.setApproved(institution.getIsApproved());
        response.setApprovedBy(institution.getApprovedBy());
        response.setApprovalDate(institution.getApprovalDate());
        response.setApprovalNotes(institution.getApprovalNotes());
        response.setRejectedBy(institution.getRejectedBy());
        response.setRejectionReason(institution.getRejectionReason());
        response.setRejectionDate(institution.getRejectionDate());
        response.setSuspendedBy(institution.getSuspendedBy());
        response.setSuspendedReason(institution.getSuspendedReason());
        response.setSuspendedDate(institution.getSuspendedDate());
        response.setEmail(institution.getEmail());
        response.setPhone(institution.getPhone());
        response.setTeacherName(institution.getTeacherName());
        response.setTeacherDesignation(institution.getTeacherDesignation());
        response.setAboutInstitution(institution.getAboutInstitution());
        response.setVillageOrHouse(institution.getVillageOrHouse());

        // Manual mapping for location objects
        if (institution.getDivision() != null) {
            response.setDivision(mapToDivisionDTO(institution.getDivision()));
        }

        if (institution.getDistrict() != null) {
            response.setDistrict(mapToDistrictDTO(institution.getDistrict()));
        }

        if (institution.getThana() != null) {
            response.setThana(mapToThanaDTO(institution.getThana()));
        }

        if (institution.getUnionOrArea() != null) {
            response.setUnionOrArea(mapToUnionOrAreaDTO(institution.getUnionOrArea()));
        }

        return response;
    }

    private DivisionResponseDTO mapToDivisionDTO(Divisions division) {
        DivisionResponseDTO dto = new DivisionResponseDTO();
        dto.setDivisionId(division.getDivisionId());
        dto.setDivisionName(division.getDivisionName());
        // Don't include districts list to avoid circular reference
        // dto.setDistricts(new ArrayList<>());
        return dto;
    }

    private DistrictResponseDTO mapToDistrictDTO(Districts district) {
        DistrictResponseDTO dto = new DistrictResponseDTO();
        dto.setDistrictId(district.getDistrictId());
        dto.setDistrictName(district.getDistrictName());

        if (district.getDivision() != null) {
            dto.setDivisionId(district.getDivision().getDivisionId());
            dto.setDivisionName(district.getDivision().getDivisionName());
        }

        // Empty thanas set - avoid lazy loading issues
        dto.setThanas(new HashSet<>());
        return dto;
    }

    private ThanaResponseDTO mapToThanaDTO(Thanas thana) {
        ThanaResponseDTO dto = new ThanaResponseDTO();
        dto.setThanaId(thana.getThanaId());
        dto.setThanaName(thana.getThanaName());

        if (thana.getDistrict() != null) {
            dto.setDistrictId(thana.getDistrict().getDistrictId());
            dto.setDistrictName(thana.getDistrict().getDistrictName());

            if (thana.getDistrict().getDivision() != null) {
                dto.setDivisionId(thana.getDistrict().getDivision().getDivisionId());
                dto.setDivisionName(thana.getDistrict().getDivision().getDivisionName());
            }
        }

        // Empty unionOrAreas list
        dto.setUnionOrAreas(new ArrayList<>());
        return dto;
    }

    private UnionOrAreaResponseDTO mapToUnionOrAreaDTO(UnionOrArea unionOrArea) {
        UnionOrAreaResponseDTO dto = new UnionOrAreaResponseDTO();
        dto.setUnionOrAreaId(unionOrArea.getUnionOrAreaId());
        dto.setUnionOrAreaName(unionOrArea.getUnionOrAreaName());

        if (unionOrArea.getThana() != null) {
            dto.setThanaId(unionOrArea.getThana().getThanaId());
            dto.setThanaName(unionOrArea.getThana().getThanaName());

            if (unionOrArea.getThana().getDistrict() != null) {
                dto.setDistrictId(unionOrArea.getThana().getDistrict().getDistrictId());
                dto.setDistrictName(unionOrArea.getThana().getDistrict().getDistrictName());

                if (unionOrArea.getThana().getDistrict().getDivision() != null) {
                    dto.setDivisionId(unionOrArea.getThana().getDistrict().getDivision().getDivisionId());
                    dto.setDivisionName(unionOrArea.getThana().getDistrict().getDivision().getDivisionName());
                }
            }
        }

        return dto;
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
    @Transactional()
    public InstitutionResponseDto getInstitutionById(Long id) {
//        Institutions institution = institutionRepository.findById(id)
//                .orElseThrow(() -> new RuntimeException("Institution not found with id " + id));
//
//        return modelMapper.map(institution, InstitutionResponseDto.class);

        Institutions institution = institutionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Institution not found with id " + id));

        // Map basic properties
        InstitutionResponseDto dto = modelMapper.map(institution, InstitutionResponseDto.class);

        // Manually set the location IDs
        if (institution.getDivision() != null) {
            dto.setDivisionId(institution.getDivision().getDivisionId());
        }

        if (institution.getDistrict() != null) {
            dto.setDistrictId(institution.getDistrict().getDistrictId());
        }

        if (institution.getThana() != null) {
            dto.setThanaId(institution.getThana().getThanaId());
        }

        if (institution.getUnionOrArea() != null) {
            dto.setUnionOrAreaId(institution.getUnionOrArea().getUnionOrAreaId());
        }

        return dto;
    }
   @Transactional
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

//    @Override
//    public List<InstitutionResponseDto> getApprovedInstitutions() {
//        return institutionRepository.findByApprovedByTrue()
//                .stream()
//                .map(institution -> modelMapper.map(institution, InstitutionResponseDto.class))
//                .collect(Collectors.toList());
//    }

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





// Institution Satus Update Method

    @Override
    @Transactional
    public InstitutionResponseDto approveInstitution(Long institutionId, Long adminId, String approvalNotes) {
        log.info("=== APPROVE INSTITUTION START ===");
        log.info("Institution ID: {}, Admin ID: {}", institutionId, adminId);

        Institutions institution = institutionRepository.findById(institutionId)
                .orElseThrow(() -> new ResourceNotFoundException("Institution not found with ID: " + institutionId));

        SystemAdmin admin = adminRepository.findById(adminId)
                .orElseThrow(() -> new ResourceNotFoundException("Admin not found with ID: " + adminId));

        // ✅ Log current state
        log.info("Current status: {}", institution.getStatus());
        log.info("Current status class: {}", institution.getStatus() != null ? institution.getStatus().getClass() : "null");
        log.info("Current isApproved: {}", institution.getIsApproved());

        // ✅ Log what we're about to set
        log.info("Setting status to enum: {}", InstitutionStatus.APPROVED);
        log.info("Enum name: {}, length: {}",
                InstitutionStatus.APPROVED.name(),
                InstitutionStatus.APPROVED.name().length());

        // Use the helper method from entity
        institution.approve(admin, approvalNotes);

        // ✅ Log after setting
        log.info("After approve() - Status: {}", institution.getStatus());
        log.info("After approve() - Status type: {}",
                institution.getStatus() != null ? institution.getStatus().getClass().getName() : "null");

        try {
            Institutions savedInstitution = institutionRepository.save(institution);
            log.info("✅ Save successful!");
            log.info("Saved institution status: {}", savedInstitution.getStatus());

            // Send approval notification
            notificationService.sendInstitutionApprovedNotification(savedInstitution, admin);
            log.info("Notification sent for institution {} approved by admin {}",
                    institutionId, adminId);

            log.info("=== APPROVE INSTITUTION END ===");
            return convertToDto(savedInstitution);

        } catch (DataIntegrityViolationException e) {
            log.error("❌ DataIntegrityViolationException: {}", e.getMessage());
            log.error("❌ Root cause: {}", getRootCause(e).getMessage());
            throw new DataTruncationException("Failed to save institution: " + getRootCause(e).getMessage());
        } catch (Exception e) {
            log.error("❌ Unexpected error: {}", e.getMessage(), e);
            throw e;
        }
    }
    private Throwable getRootCause(Throwable throwable) {
        Throwable rootCause = throwable;
        while (rootCause.getCause() != null && rootCause.getCause() != rootCause) {
            rootCause = rootCause.getCause();
        }
        return rootCause;
    }



@Override
public List<InstitutionResponseDto> getApprovedInstitutions() {
    return institutionRepository.findByStatus(InstitutionStatus.APPROVED)
            .stream()
            .map(this::convertToResponseDto)
            .collect(Collectors.toList());
}

    @Override
    @Transactional
    public InstitutionResponseDto activateInstitution(Long institutionId, Long adminId) {
        Institutions institution = institutionRepository.findById(institutionId)
                .orElseThrow(() -> new ResourceNotFoundException("Institution not found"));

        SystemAdmin admin = adminRepository.findById(adminId)
                .orElseThrow(() -> new ResourceNotFoundException("Admin not found"));

        institution.setStatus(InstitutionStatus.ACTIVE);
        institution.setUpdateDate(LocalDateTime.now());

        Institutions savedInstitution = institutionRepository.save(institution);

        // Send activation notification
        notificationService.createInstitutionNotification(
                savedInstitution,
                "Account Activated",
                String.format("Your account has been activated by Admin %s. You can now login.",
                        admin.getName()),
                NotificationType.INSTITUTION_APPROVED,
                "/login",
                "INSTITUTION",
                savedInstitution.getInstitutionsId()
        );

        log.info("Institution " + institutionId + " activated by admin " + adminId);

        return convertToDto(savedInstitution);
    }
    @Override
    @Transactional
    public InstitutionResponseDto rejectInstitution(Long institutionId, Long adminId, String rejectionReason) {
        // Find institution
        Institutions institution = institutionRepository.findById(institutionId)
                .orElseThrow(() -> new ResourceNotFoundException("Institution not found with ID: " + institutionId));

        // Find admin
        SystemAdmin admin = systemAdminRepository.findById(adminId)
                .orElseThrow(() -> new ResourceNotFoundException("Admin not found with ID: " + adminId));

        // Reject institution
        institution.setStatus(InstitutionStatus.REJECTED);
        institution.setRejectionReason(rejectionReason);
        institution.setRejectedBy(admin);
        institution.setRejectionDate(LocalDateTime.now());
        institution.setUpdateDate(LocalDateTime.now());

        Institutions savedInstitution = institutionRepository.save(institution);

        // Send rejection notification
        notificationService.sendInstitutionRejectedNotification(savedInstitution, admin, rejectionReason);


        log.info("Institution " + institutionId + " rejected by admin " + adminId);
        return convertToResponseDto(savedInstitution);
    }
//    @Override
//    public InstitutionResponseDto rejectInstitutionAndGet(Long institutionId, Long adminId, String rejectionReason) {
//        // This is an alias for rejectInstitution method
//        return rejectInstitution(institutionId, adminId, rejectionReason);
//    }

    // Helper methods

    private InstitutionResponseDto convertToResponseDto(Institutions institution) {
        InstitutionResponseDto dto = modelMapper.map(institution, InstitutionResponseDto.class);

        // Set location IDs
        if (institution.getDivision() != null) {
            dto.setDivisionId(institution.getDivision().getDivisionId());
        }
        if (institution.getDistrict() != null) {
            dto.setDistrictId(institution.getDistrict().getDistrictId());
        }
        if (institution.getThana() != null) {
            dto.setThanaId(institution.getThana().getThanaId());
        }
        if (institution.getUnionOrArea() != null) {
            dto.setUnionOrAreaId(institution.getUnionOrArea().getUnionOrAreaId());
        }

        // Set admin information if available
        if (institution.getApprovedBy() != null) {
            dto.setApproved(true);
            dto.setApprovedBy(institution.getApprovedBy());
        }

        return dto;
    }
    @Override
    @Transactional
    public InstitutionResponseDto suspendInstitution(Long institutionId, Long adminId, String suspensionReason) {
        // Find institution
        Institutions institution = institutionRepository.findById(institutionId)
                .orElseThrow(() -> new ResourceNotFoundException("Institution not found with ID: " + institutionId));

        // Find admin
        SystemAdmin admin = systemAdminRepository.findById(adminId)
                .orElseThrow(() -> new ResourceNotFoundException("Admin not found with ID: " + adminId));

        // Suspend institution
        institution.setStatus(InstitutionStatus.SUSPENDED);
        institution.setSuspendedReason(suspensionReason);
        institution.setSuspendedBy(admin);
        institution.setSuspendedDate(LocalDateTime.now());
        institution.setUpdateDate(LocalDateTime.now());

        Institutions savedInstitution = institutionRepository.save(institution);

        // Send suspension notification
        notificationService.createInstitutionNotification(
                savedInstitution,
                "Account Suspended",
                String.format("Your account has been suspended by Admin %s. Reason: %s",
                        admin.getName(), suspensionReason),
                NotificationType.INSTITUTION_SUSPENDED,
                "/contact-support",
                "INSTITUTION",
                savedInstitution.getInstitutionsId()
        );
        log.info("Institution " + institutionId + " suspended by admin " + adminId);

        return convertToResponseDto(savedInstitution);
    }

    // InstitutionServiceImpl.java


        @Override
        @Transactional()
        public StatusStatisticsDto getStatusStatistics() {
            log.info("Fetching institution status statistics");

            try {
                // Method 1: Using individual counts (more reliable)
                Long total = countAllInstitutions();
               Long approved = institutionRepository.countByStatus(InstitutionStatus.APPROVED);
               // Long pending = institutionRepository.countByStatus(InstitutionStatus.PENDING);
                Long rejected = institutionRepository.countByStatus(InstitutionStatus.REJECTED);
                Long suspended = institutionRepository.countByStatus(InstitutionStatus.SUSPENDED);

                // Method 2: Alternative using isApproved field
                // Long approved = institutionRepository.countByIsApproved(true);
                 Long pending = total - approved - rejected - suspended;

                StatusStatisticsDto stats = new StatusStatisticsDto();
                stats.setTotal(total != null ? total : 0L);
                stats.setApproved(approved != null ? approved : 0L);
                stats.setPending(pending != null ? pending : 0L);
                stats.setRejected(rejected != null ? rejected : 0L);
                stats.setSuspended(suspended != null ? suspended : 0L);

                log.info("Statistics fetched - Total: {}, Approved: {}, Pending: {}, Rejected: {}, Suspended: {}",
                        stats.getTotal(), stats.getApproved(), stats.getPending(),
                        stats.getRejected(), stats.getSuspended());

                return stats;

            } catch (Exception e) {
                log.error("Error fetching status statistics: {}", e.getMessage());
                // Return empty statistics in case of error
                return new StatusStatisticsDto(0L, 0L, 0L, 0L, 0L);
            }
        }

        // Helper method if countAll() doesn't exist
        private Long countAllInstitutions() {
            return institutionRepository.count();
        }



}
