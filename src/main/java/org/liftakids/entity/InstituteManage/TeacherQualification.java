package org.liftakids.entity.InstituteManage;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "teacher_qualifications")
public class TeacherQualification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "teacher_id", nullable = false)
    private Teacher teacher;

    @Column(length = 100)
    private String degree;

    @Column(length = 255)
    private String institution;

    private Integer year;

    @Column(length = 50)
    private String grade;

    @Column(name = "is_highest")
    private boolean isHighest = false;

    @Column(columnDefinition = "TEXT")
    private String description;
}
