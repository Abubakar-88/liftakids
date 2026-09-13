package org.liftakids.repositories.instituteManage;

import org.liftakids.entity.InstituteManage.TeacherSchedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TeacherScheduleRepository extends JpaRepository<TeacherSchedule, Long> {

    List<TeacherSchedule> findByTeacher_Id(Long teacherId);

    List<TeacherSchedule> findByTeacher_IdAndDay(Long teacherId, String day);

    List<TeacherSchedule> findByTeacher_IdAndIsActiveTrue(Long teacherId);

    void deleteByTeacher_Id(Long teacherId);

    void deleteByTeacher_IdAndDay(Long teacherId, String day);
}
