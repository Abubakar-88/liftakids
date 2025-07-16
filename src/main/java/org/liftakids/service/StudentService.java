package org.liftakids.service;

import org.liftakids.dto.student.StudentRequestDto;
import org.liftakids.dto.student.StudentResponseDto;

import java.util.List;

public interface StudentService {
    StudentResponseDto createStudent(StudentRequestDto requestDto);
    StudentResponseDto getStudentById(Long id);
    List<StudentResponseDto> getAllStudents();
}
