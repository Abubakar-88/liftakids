package org.liftakids.entity.InstituteManage;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "teacher_class_assignments")
public class TeacherClassAssignment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "teacher_id", nullable = false)
    private Teacher teacher;

    @Column(name = "class_name", nullable = false, length = 20)
    private String className; // "Class 1", "Class 2", ..., "Class 6"

    @Column(name = "section", length = 10)
    private String section; // "A", "B", or null

    @Column(name = "subject", nullable = false, length = 100)
    private String subject; // "Mathematics", "Science", etc.

    @Column(name = "academic_year")
    private String academicYear; // "2024-2025"

    @Column(name = "is_class_teacher")
    private boolean isClassTeacher = false;

    @Column(name = "is_active")
    private boolean isActive = true;

    @Column(name = "assigned_at")
    private LocalDateTime assignedAt;

    @PrePersist
    protected void onCreate() {
        assignedAt = LocalDateTime.now();
        if (academicYear == null) {
            int year = LocalDate.now().getYear();
            academicYear = year + "-" + (year + 1);
        }
    }
}