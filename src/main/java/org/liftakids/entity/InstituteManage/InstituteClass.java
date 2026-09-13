package org.liftakids.entity.InstituteManage;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.liftakids.entity.Institutions;
import org.liftakids.entity.Student;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "institute_classes")
public class InstituteClass {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    private String className; // "Class 1", "Class 2", etc.

    @Column(length = 20)
    private String section; // "A", "B", or null

    @Column(nullable = false)
    private Integer classOrder; // 1, 2, 3, etc. for sorting

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "institution_id", nullable = false)
    private Institutions institution;

    @OneToMany(mappedBy = "instituteClass", cascade = CascadeType.ALL)
    private List<Student> students = new ArrayList<>();

    @Column(nullable = false)
    private boolean isActive = true;

    @Column(name = "academic_year")
    private String academicYear;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        if (academicYear == null) {
            int year = LocalDate.now().getYear();
            academicYear = year + "-" + (year + 1);
        }
    }
}