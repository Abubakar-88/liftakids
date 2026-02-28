package org.liftakids.service.impl;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.liftakids.dto.institute.InstitutionResponseDto;
import org.liftakids.dto.student.StudentRequestDto;
import org.liftakids.dto.student.StudentResponseDto;
import org.liftakids.dto.student.StudentUpdateRequestDTO;
import org.liftakids.entity.*;
import org.liftakids.exception.ResourceNotFoundException;
import org.liftakids.repositories.InstitutionRepository;
import org.liftakids.repositories.SponsorshipRepository;
import org.liftakids.repositories.StudentRepository;
import org.liftakids.service.S3Service;
import org.liftakids.service.StudentService;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StudentServiceImpl implements StudentService {

    private final StudentRepository studentRepository;
    private final InstitutionRepository institutionRepository;
    private final ModelMapper modelMapper;
    private final S3Service s3Service;
    private final SponsorshipRepository sponsorshipRepository;
    private final Logger log = LoggerFactory.getLogger(StudentService.class);

    @Transactional
    @Override
    public StudentResponseDto createStudent(StudentRequestDto requestDto, MultipartFile image) throws IOException {

        Student student = modelMapper.map(requestDto, Student.class);
        student.setSponsored(false);
        Institutions institution = institutionRepository.findById(requestDto.getInstitutionsId())
                .orElseThrow(() -> new ResourceNotFoundException("Institution not found"));

        student.setInstitution(institution);
        student.setStudentId(null);
        // Save the image to Cloudflare R2 and get the file URL
        if (!image.isEmpty()) {
            String photoUrl = s3Service.uploadFile(image, student.getStudentName()); // Use S3Service to upload
            student.setPhotoUrl(photoUrl); // Set the image URL in the item
        }

        Student saved = studentRepository.save(student);

        StudentResponseDto response = modelMapper.map(saved, StudentResponseDto.class);
        response.setInstitutionName(institution.getInstitutionName());
        return response;

    }

    @Transactional
    public void updateStudentSponsorshipStatus(Long studentId) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found"));
        student.updateSponsorshipStatus();
        studentRepository.save(student);
    }
    @Override
    @Transactional
    public StudentResponseDto updateStudent(Long studentId, StudentUpdateRequestDTO updateRequest, MultipartFile image) throws IOException {
        Student existingStudent  = studentRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with id: " + studentId));

        // Update student fields
        existingStudent .setStudentName(updateRequest.studentName());
        existingStudent .setContactNumber(updateRequest.contactNumber());
        existingStudent .setDob(updateRequest.dob());
        existingStudent .setBio(updateRequest.bio());
        existingStudent .setFinancial_rank(FinancialRank.valueOf(updateRequest.financial_rank()));
        existingStudent .setRequiredMonthlySupport(updateRequest.requiredMonthlySupport());
        existingStudent .setAddress(updateRequest.address());
        existingStudent .setGuardianName(updateRequest.guardianName());
        // Update institution if changed



  // Handle image update
        if (image != null && !image.isEmpty()) {
            // Delete old image from Cloudflare R2 if exists
            if (existingStudent.getPhotoUrl() != null) {
                s3Service.deleteFile(existingStudent.getPhotoUrl());
            }

            // Upload new image
            String photoUrl = s3Service.uploadFile(image, existingStudent.getStudentName());
            existingStudent.setPhotoUrl(photoUrl);
        }
        Student updatedStudent = studentRepository.save(existingStudent );
        return modelMapper.map(updatedStudent, StudentResponseDto.class);
    }

    @Transactional
    @Override
    public StudentResponseDto getStudentById(Long id) {
        Student student = studentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Student not found"));

        StudentResponseDto response = modelMapper.map(student, StudentResponseDto.class);
        response.setInstitutionsId(student.getInstitution().getInstitutionsId());
        response.setSponsored(student.isSponsored());
        response.setInstitutionPhone(student.getInstitution().getPhone());
        response.setInstitutionName(student.getInstitution().getInstitutionName());
        // Also set the complete InstitutionResponseDto
        InstitutionResponseDto institutionDto = modelMapper.map(student.getInstitution(), InstitutionResponseDto.class);
        response.setInstitutions(institutionDto);
      //  response.setFullySponsored(student.isFullySponsored());
        // Calculate sponsorship details
        BigDecimal required = student.getRequiredMonthlySupport() != null ?
                student.getRequiredMonthlySupport() : BigDecimal.ZERO;
        BigDecimal sponsored = student.getCurrentSponsorships().stream()
                .map(Sponsorship::getMonthlyAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        response.setFullySponsored(sponsored.compareTo(required) >= 0);
        response.setSponsoredAmount(sponsored);
        return response;
    }
    @Transactional
    @Override
    public List<StudentResponseDto> getAllStudents() {
        return studentRepository.findAll().stream()
                .map(student -> {
                    StudentResponseDto dto = modelMapper.map(student, StudentResponseDto.class);
                    dto.setInstitutionsId(student.getInstitution().getInstitutionsId());
                    dto.setSponsored(student.isSponsored());
                    // Also set the complete InstitutionResponseDto
                    InstitutionResponseDto institutionDto = modelMapper.map(student.getInstitution(), InstitutionResponseDto.class);
                    dto.setInstitutions(institutionDto);

                    return dto;
                })
                .collect(Collectors.toList());
    }
    private int calculateAge(Date dob) {
        if (dob == null) return 0;
        return java.time.Period.between(
                dob.toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDate(),
                LocalDate.now()
        ).getYears();
    }
    @Transactional
    @Override
    public Page<StudentResponseDto> getAllStudents(Pageable pageable) {
        Page<Student> studentPage = studentRepository.findAllWithSponsorships(pageable);

        return studentPage.map(student -> {
            StudentResponseDto dto = modelMapper.map(student, StudentResponseDto.class);
            dto.setInstitutionPhone(student.getInstitution().getPhone());
            dto.setInstitutionName(student.getInstitution().getInstitutionName());
            // Also set the complete InstitutionResponseDto
            InstitutionResponseDto institutionDto = modelMapper.map(student.getInstitution(), InstitutionResponseDto.class);
            dto.setInstitutions(institutionDto);
            // Handle institution data
            if (student.getInstitution() != null) {
                dto.setInstitutionsId(student.getInstitution().getInstitutionsId());
                dto.setInstitutionName(student.getInstitution().getInstitutionName());
            }

            // Calculate sponsorship status
            boolean isSponsored = !student.getCurrentSponsorships().isEmpty();
            dto.setSponsored(isSponsored);

            // Calculate financial information
            BigDecimal required = student.getRequiredMonthlySupport() != null ?
                    student.getRequiredMonthlySupport() : BigDecimal.ZERO;
            BigDecimal sponsoredAmount = isSponsored ?
                    student.getCurrentSponsorships().stream()
                            .map(Sponsorship::getTotalPaidAmount)
                            .reduce(BigDecimal.ZERO, BigDecimal::add) :
                    BigDecimal.ZERO;

            dto.setSponsoredAmount(sponsoredAmount);
            dto.setFullySponsored(sponsoredAmount.compareTo(required) >= 0);

            // Map sponsors if they exist
            if (isSponsored) {
                dto.setSponsors(student.getCurrentSponsorships().stream()
                        .map(sponsorship -> {
                            Donor donor = sponsorship.getDonor();
                            return StudentResponseDto.SponsorInfoDto.builder()
                                    .donorId(donor.getDonorId())
                                    .donorName(donor.getName())
                                    .monthlyAmount(sponsorship.getMonthlyAmount())
                                    .startDate(sponsorship.getStartDate())
                                    .endDate(sponsorship.getEndDate())
                                    .status(sponsorship.getStatus())
                                    .lastPaymentDate(sponsorship.getLastPaymentDate())
                                    .paidUpTo(sponsorship.getPaidUpTo())
                                    .monthsPaid(sponsorship.getMonthsPaid())
                                    .totalAmount(sponsorship.getTotalAmount())
                                    .nextPaymentDueDate(sponsorship.getNextPaymentDueDate())
                                    .build();
                        })
                        .collect(Collectors.toList()));
            } else {
                dto.setSponsors(Collections.emptyList());
            }

            return dto;
        });
    }
//    @Override
//    public Page<StudentResponseDto> getAllStudents(Pageable pageable) {
//        Page<Student> studentPage = studentRepository.findAll(pageable);
//
//        return studentPage.map(student -> {
//            StudentResponseDto dto = modelMapper.map(student, StudentResponseDto.class);
//            dto.setInstitutionsId(student.getInstitution().getInstitutionsId());
//            dto.setSponsored(student.isSponsored());
//            return dto;
//        });
//    }
@Transactional
@Override
public List<StudentResponseDto> getStudentsByInstitution(Long institutionId) {
    // Check if institution exists
    Institutions institution = institutionRepository.findById(institutionId)
            .orElseThrow(() -> new ResourceNotFoundException("Institution not found with ID: " + institutionId));

    // Fetch students associated with the institution
    List<Student> students = studentRepository.findByInstitution(institution);

    if (students.isEmpty()) {
        throw new ResourceNotFoundException("No students found for Institution ID: " + institutionId);
    }

    // Convert to DTOs
    return students.stream()
            .map(this::convertToStudentResponseDto)
            .collect(Collectors.toList());
}
    @Transactional
    @Override
    public Page<StudentResponseDto> getStudentsByInstitution(Long institutionId, Pageable pageable) {
        // Verify institution exists
        if (!institutionRepository.existsById(institutionId)) {
            throw new ResourceNotFoundException("Institution not found with id: " + institutionId);
        }

        Page<Student> studentsPage = studentRepository.findByInstitution_InstitutionsId(institutionId, pageable);

        // Convert to DTO with pagination
        return studentsPage.map(this::convertToStudentResponseDto);
    }
    @Override
    @Transactional()
    public List<StudentResponseDto> getStudentPendingSponsorships(Long studentId, LocalDate fromDate) {

        try {
            // 1. Check if student exists
            Student student = studentRepository.findById(studentId)
                    .orElseThrow(() -> new ResourceNotFoundException("Student not found with id: " + studentId));

            // 2. Fetch pending sponsorships
            List<Sponsorship> pendingSponsorships = sponsorshipRepository
                    .findByStudentStudentIdAndStatusAndSponsorStartDateAfter(
                            studentId,
                            SponsorshipStatus.PENDING_PAYMENT,
                            fromDate
                    );

            if (pendingSponsorships.isEmpty()) {
                return Collections.emptyList();
            }

            // 3. Convert to DTO (using a dedicated method for pending sponsorships)
            StudentResponseDto studentDto = convertToStudentResponseDtoWithPendingSponsorships(student, pendingSponsorships);

            return Collections.singletonList(studentDto);

        } catch (ResourceNotFoundException e) {
            log.warn("Student not found: {}", studentId);
            throw e;
        } catch (Exception e) {
            log.error("Error fetching pending sponsorships for student {}: {}", studentId, e.getMessage(), e);
            throw new RuntimeException("Failed to fetch pending sponsorships", e);
        }
    }
    @Override
    @Transactional()
    public boolean hasPendingSponsorships(Long studentId, LocalDate fromDate) {
        try {
            long count = sponsorshipRepository.countByStudentStudentIdAndStatusAndSponsorStartDateAfter(
                    studentId,
                    SponsorshipStatus.PENDING_PAYMENT,
                    fromDate
            );
            return count > 0;
        } catch (Exception e) {
            log.error("Error checking pending sponsorships for student {}: {}", studentId, e.getMessage(), e);
            return false;
        }
    }
    // Method for converting student with ONLY pending sponsorships
    private StudentResponseDto convertToStudentResponseDtoWithPendingSponsorships(
            Student student, List<Sponsorship> pendingSponsorships) {

        StudentResponseDto dto = new StudentResponseDto();

        // Set basic student info
        dto.setStudentId(student.getStudentId());
        dto.setStudentName(student.getStudentName());
        dto.setRequiredMonthlySupport(student.getRequiredMonthlySupport());

        // Set institution information
        Institutions institution = student.getInstitution();
        dto.setInstitutionsId(institution.getInstitutionsId());
        dto.setInstitutionName(institution.getInstitutionName());

        // Set complete InstitutionResponseDto
        InstitutionResponseDto institutionDto = modelMapper.map(institution, InstitutionResponseDto.class);
        dto.setInstitutions(institutionDto);

        // Convert ONLY pending sponsorships
        List<StudentResponseDto.SponsorInfoDto> pendingSponsorInfoList = pendingSponsorships.stream()
                .map(sponsorship -> convertToSponsorInfoDto(sponsorship))
                .collect(Collectors.toList());

        dto.setSponsors(pendingSponsorInfoList);

        // Calculate total sponsored amount from COMPLETED sponsorships only
        BigDecimal totalSponsoredAmount = calculateTotalSponsoredAmount(student);
        dto.setSponsoredAmount(totalSponsoredAmount);
        dto.setFullySponsored(totalSponsoredAmount.compareTo(student.getRequiredMonthlySupport()) >= 0);
        dto.setSponsored(!pendingSponsorInfoList.isEmpty() || totalSponsoredAmount.compareTo(BigDecimal.ZERO) > 0);

        return dto;
    }

    // Helper method to convert Sponsorship to SponsorInfoDto
    private StudentResponseDto.SponsorInfoDto convertToSponsorInfoDto(Sponsorship sponsorship) {
        return StudentResponseDto.SponsorInfoDto.builder()
                .donorId(sponsorship.getDonor().getDonorId())
                .donorName(sponsorship.getDonor().getName())
                .monthlyAmount(sponsorship.getMonthlyAmount())
                .sponsorStartDate(sponsorship.getSponsorStartDate())
                .startDate(sponsorship.getStartDate())
                .endDate(sponsorship.getEndDate())
                .status(sponsorship.getStatus())
                .lastPaymentDate(sponsorship.getLastPaymentDate())
                .paidUpTo(sponsorship.getPaidUpTo())
                .monthsPaid(sponsorship.getMonthsPaid())
                .totalMonths(sponsorship.getTotalMonths())
                .totalAmount(sponsorship.getTotalAmount())
                .nextPaymentDueDate(sponsorship.getNextPaymentDueDate())
                .build();
    }

    // Calculate total sponsored amount from COMPLETED sponsorships
    private BigDecimal calculateTotalSponsoredAmount(Student student) {
        if (student.getCurrentSponsorships() == null) {
            return BigDecimal.ZERO;
        }

        return student.getCurrentSponsorships().stream()
                .filter(sponsorship -> sponsorship.getStatus() == SponsorshipStatus.COMPLETED)
                .map(Sponsorship::getTotalPaidAmount)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    // Original method for general student conversion (unchanged)
    public StudentResponseDto convertToStudentResponseDto(Student student) {
        StudentResponseDto dto = modelMapper.map(student, StudentResponseDto.class);

        // Set institution information
        dto.setInstitutionsId(student.getInstitution().getInstitutionsId());
        dto.setInstitutionName(student.getInstitution().getInstitutionName());

        // Also set the complete InstitutionResponseDto
        InstitutionResponseDto institutionDto = modelMapper.map(student.getInstitution(), InstitutionResponseDto.class);
        dto.setInstitutions(institutionDto);

        // Calculate sponsorship details
        BigDecimal totalSponsoredAmount = BigDecimal.ZERO;
        List<StudentResponseDto.SponsorInfoDto> sponsorInfoList = new ArrayList<>();

        if (student.getCurrentSponsorships() != null) {
            for (Sponsorship sponsorship : student.getCurrentSponsorships()) {
                if (sponsorship.getStatus() == SponsorshipStatus.COMPLETED) {
                    totalSponsoredAmount = totalSponsoredAmount.add(sponsorship.getTotalPaidAmount());

                    StudentResponseDto.SponsorInfoDto sponsorInfo = convertToSponsorInfoDto(sponsorship);
                    sponsorInfoList.add(sponsorInfo);
                }
            }
        }

        dto.setSponsoredAmount(totalSponsoredAmount);
        dto.setSponsors(sponsorInfoList);
        dto.setFullySponsored(totalSponsoredAmount.compareTo(student.getRequiredMonthlySupport()) >= 0);
        dto.setSponsored(!sponsorInfoList.isEmpty());

        return dto;
    }
    @Transactional
    @Override
    public List<StudentResponseDto> searchStudentsByInstitution(
            Long institutionId,
            String studentName,
            String guardianName,
            String contactNumber) {

        // Verify institution exists
        if (!institutionRepository.existsById(institutionId)) {
            throw new ResourceNotFoundException("Institution not found with id: " + institutionId);
        }

        List<Student> students = studentRepository.findByInstitutionWithFilters(
                institutionId, studentName, guardianName, contactNumber);

        return students.stream()
                .map(this::convertToStudentResponseDto)
                .collect(Collectors.toList());
    }


    @Transactional
    @Override
    public List<StudentResponseDto> searchStudents(String studentName, String guardianName, String gender, String contactNumber) {
        List<Student> students = studentRepository.searchStudents(studentName, guardianName,contactNumber);
        return students.stream()
                .map(student -> {
                    StudentResponseDto dto = modelMapper.map(student, StudentResponseDto.class);
                    dto.setInstitutionsId(student.getInstitution().getInstitutionsId());
                    return dto;
                })
                .collect(Collectors.toList());
    }

//    @Override
//    public void deleteStudent(Long studentId) {
//        if (!studentRepository.existsById(studentId)) {
//            throw new ResourceNotFoundException("Student not found with id: " + studentId);
//        }
//        studentRepository.deleteById(studentId);
//    }
@Override
public void deleteStudent(Long studentId) {
    Student student = studentRepository.findById(studentId)
            .orElseThrow(() -> new ResourceNotFoundException("Student not found"));

    // Delete image from S3 if exists
    if (student.getPhotoUrl() != null && !student.getPhotoUrl().isEmpty()) {
        s3Service.deleteFile(student.getPhotoUrl());
    }

    // Delete student record
    studentRepository.delete(student);
}

    @Transactional
    @Override
    public List<StudentResponseDto> getTop3UnsponsoredUrgentStudents() {
        List<Student> students = studentRepository.findTop3UnsponsoredUrgentStudents();
        return students.stream()
                .map(this::convertToStudentResponseDto)
                .collect(Collectors.toList());
    }

//    @Override
//    public List<StudentResponseDto> getUnsponsoredStudentsByFinancialRank(String financialRank, int limit) {
//        List<Student> students = studentRepository.findTopNBySponsoredFalseAndFinancialRankAndStatus(
//                financialRank, StudentStatus.ACTIVE.name(), limit);
//
//        return students.stream()
//                .map(StudentResponseDto::fromEntity)
//                .collect(Collectors.toList());
//    }
}
