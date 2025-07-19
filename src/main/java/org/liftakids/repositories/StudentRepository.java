package org.liftakids.repositories;

import org.liftakids.entity.Student;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface StudentRepository extends JpaRepository<Student,Long> {
    Optional<Student> findByStudentNameOrContactNumber(String studentName, String contactNumber);
}
