package org.liftakids.service.impl;

import lombok.RequiredArgsConstructor;
import org.liftakids.dto.student.StudentRequestDto;
import org.liftakids.dto.student.StudentResponseDto;
import org.liftakids.entity.Institutions;
import org.liftakids.entity.Student;

import org.liftakids.repositories.InstitutionRepository;
import org.liftakids.repositories.StudentRepository;
import org.liftakids.service.StudentService;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

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
        int age = calculateAge(requestDto.getDob());
        if (age <= 4) {
            throw new RuntimeException("Student must be older than 4 years.");
        }
        Student student = modelMapper.map(requestDto, Student.class);

        Institutions institution = institutionRepository.findById(requestDto.getInstitutionId())
                .orElseThrow(() -> new RuntimeException("Institution not found"));

        student.setInstitution(institution);
        student.setStudentId(null); // ensure creation

        Student saved = studentRepository.save(student);

        StudentResponseDto response = modelMapper.map(saved, StudentResponseDto.class);
        response.setInstitutionId(institution.getInstitutionsId());


        return response;
    }

    @Override
    public StudentResponseDto getStudentById(Long id) {
        Student student = studentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Student not found"));

        StudentResponseDto response = modelMapper.map(student, StudentResponseDto.class);
        response.setInstitutionId(student.getInstitution().getInstitutionsId());
      //  response.setFullySponsored(student.isFullySponsored());

        return response;
    }

    @Override
    public List<StudentResponseDto> getAllStudents() {
        return studentRepository.findAll().stream()
                .map(student -> {
                    StudentResponseDto dto = modelMapper.map(student, StudentResponseDto.class);
                    dto.setInstitutionId(student.getInstitution().getInstitutionsId());
                 //   dto.setFullySponsored(student.isFullySponsored());
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
}
