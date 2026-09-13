package org.liftakids.entity.InstituteManage;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "teacher_schedules")
public class TeacherSchedule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "teacher_id", nullable = false)
    private Teacher teacher;

    @Column(name = "class_name", nullable = false, length = 20)
    private String className; // "Class 1", "Class 2", etc.

    @Column(name = "subject", nullable = false, length = 100)
    private String subject;

    @Column(name = "day", nullable = false, length = 15)
    private String day; // "Saturday", "Sunday", etc.

    @Column(name = "start_time")
    private LocalTime startTime;

    @Column(name = "end_time")
    private LocalTime endTime;

    @Column(name = "room", length = 50)
    private String room;

    @Column(name = "is_active")
    private boolean isActive = true;
}