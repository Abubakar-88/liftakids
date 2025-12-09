package org.liftakids.service;

import org.liftakids.dto.student.StudentRequestDto;
import org.liftakids.dto.student.StudentResponseDto;
import org.liftakids.dto.student.StudentUpdateRequestDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

public interface StudentService {
    StudentResponseDto createStudent(StudentRequestDto requestDto, MultipartFile image) throws IOException;
    StudentResponseDto updateStudent(Long studentId, StudentUpdateRequestDTO updateRequest, MultipartFile image) throws IOException;
    StudentResponseDto getStudentById(Long id);
    List<StudentResponseDto> getAllStudents();
    List<StudentResponseDto> getStudentsByInstitution(Long institutionId);
    Page<StudentResponseDto> getStudentsByInstitution(Long institutionId, Pageable pageable);
    List<StudentResponseDto> searchStudentsByInstitution(
            Long institutionId,
            String studentName,
            String guardianName,
            String contactNumber);
    List<StudentResponseDto> searchStudents(String studentName, String guardianName, String gender, String contactNumber);
    Page<StudentResponseDto> getAllStudents(Pageable pageable);
    void deleteStudent(Long studentId);
    List<StudentResponseDto> getTop3UnsponsoredUrgentStudents();
    //List<StudentResponseDto> getUnsponsoredUrgentStudents(int limit);
    List<StudentResponseDto> getStudentPendingSponsorships(Long studentId, LocalDate fromDate);
    public boolean hasPendingSponsorships(Long studentId, LocalDate fromDate);
}
