package org.liftakids.service.impl;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.liftakids.dto.student.StudentRequestDto;
import org.liftakids.dto.student.StudentResponseDto;
import org.liftakids.dto.student.StudentUpdateRequestDTO;
import org.liftakids.entity.*;

import org.liftakids.exception.ResourceNotFoundException;
import org.liftakids.repositories.InstitutionRepository;
import org.liftakids.repositories.StudentRepository;
import org.liftakids.service.StudentService;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StudentServiceImpl implements StudentService {

    private final StudentRepository studentRepository;
    private final InstitutionRepository institutionRepository;
    private final ModelMapper modelMapper;

    @Override
    public StudentResponseDto createStudent(StudentRequestDto requestDto) {

        Student student = modelMapper.map(requestDto, Student.class);
        student.setSponsored(false);
        Institutions institution = institutionRepository.findById(requestDto.getInstitutionsId())
                .orElseThrow(() -> new ResourceNotFoundException("Institution not found"));

        student.setInstitution(institution);
        student.setStudentId(null);

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
    public StudentResponseDto updateStudent(Long studentId, StudentUpdateRequestDTO updateRequest) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with id: " + studentId));

        // Update student fields
        student.setStudentName(updateRequest.studentName());
        student.setContactNumber(updateRequest.contactNumber());
        student.setDob(updateRequest.dob());
        student.setBio(updateRequest.bio());
        student.setFinancial_rank(FinancialRank.valueOf(updateRequest.financial_rank()));
        student.setRequiredMonthlySupport(updateRequest.requiredMonthlySupport());
        student.setAddress(updateRequest.address());
        student.setGuardianName(updateRequest.guardianName());

        Student updatedStudent = studentRepository.save(student);
        return modelMapper.map(updatedStudent, StudentResponseDto.class);
    }

    @Override
    public StudentResponseDto getStudentById(Long id) {
        Student student = studentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Student not found"));

        StudentResponseDto response = modelMapper.map(student, StudentResponseDto.class);
        response.setInstitutionsId(student.getInstitution().getInstitutionsId());
        response.setSponsored(student.isSponsored());
        response.setInstitutionPhone(student.getInstitution().getPhone());
        response.setInstitutionName(student.getInstitution().getInstitutionName());
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

    @Override
    public List<StudentResponseDto> getAllStudents() {
        return studentRepository.findAll().stream()
                .map(student -> {
                    StudentResponseDto dto = modelMapper.map(student, StudentResponseDto.class);
                    dto.setInstitutionsId(student.getInstitution().getInstitutionsId());
                    dto.setSponsored(student.isSponsored());
                    return dto;
                })
                .collect(Collectors.toList());
    }
    private int calculateAge(Date dob) {
        if (dob == null) return 0;
        return java.time.Period.between(
                dob.toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDate(),
                java.time.LocalDate.now()
        ).getYears();
    }

    @Override
    public Page<StudentResponseDto> getAllStudents(Pageable pageable) {
        Page<Student> studentPage = studentRepository.findAllWithSponsorships(pageable);

        return studentPage.map(student -> {
            StudentResponseDto dto = modelMapper.map(student, StudentResponseDto.class);
            dto.setInstitutionPhone(student.getInstitution().getPhone());
            dto.setInstitutionName(student.getInstitution().getInstitutionName());
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
        // Convert to DTO
        return students.stream()
                .map(student -> modelMapper.map(student, StudentResponseDto.class))
                .toList();
    }
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

    private StudentResponseDto convertToStudentResponseDto(Student student) {
        StudentResponseDto dto = modelMapper.map(student, StudentResponseDto.class);

        // Set institution information
        dto.setInstitutionsId(student.getInstitution().getInstitutionsId());
        dto.setInstitutionName(student.getInstitution().getInstitutionName());

        // Calculate sponsorship details
        BigDecimal totalSponsoredAmount = BigDecimal.ZERO;
        List<StudentResponseDto.SponsorInfoDto> sponsorInfoList = new ArrayList<>();

        if (student.getCurrentSponsorships() != null) {
            for (Sponsorship sponsorship : student.getCurrentSponsorships()) {
                if (sponsorship.getStatus() == SponsorshipStatus.COMPLETED) {
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
                            .totalMonths(sponsorship.getTotalMonths())
                            .totalAmount(sponsorship.getTotalAmount())
                            .nextPaymentDueDate(sponsorship.getNextPaymentDueDate())
                            .build();

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

    @Override
    public void deleteStudent(Long studentId) {
        if (!studentRepository.existsById(studentId)) {
            throw new ResourceNotFoundException("Student not found with id: " + studentId);
        }
        studentRepository.deleteById(studentId);
    }

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
