package org.liftakids.service;

import org.liftakids.dto.student.StudentRequestDto;
import org.liftakids.dto.student.StudentResponseDto;
import org.liftakids.dto.student.StudentUpdateRequestDTO;

import java.util.List;

public interface StudentService {
    StudentResponseDto createStudent(StudentRequestDto requestDto);
    StudentResponseDto updateStudent(Long studentId, StudentUpdateRequestDTO updateRequest);
    StudentResponseDto getStudentById(Long id);
    List<StudentResponseDto> getAllStudents();
}
