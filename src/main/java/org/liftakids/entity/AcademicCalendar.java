// AcademicCalendar.java
package org.liftakids.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "academic_calendars")
public class AcademicCalendar {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false)
    private LocalDate startDate;

    @Column(nullable = false)
    private LocalDate endDate;

    @Column(nullable = false)
    private String eventType; // EXAM, HOLIDAY, EVENT, ASSIGNMENT, MEETING, OTHER

    @Column(length = 50)
    private String eventColor; // Hex color code

    @Column(nullable = false)
    private String targetAudience; // ALL, STUDENTS, TEACHERS, ADMIN, PARENTS

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "institution_id", nullable = false)
    private Institutions institution;

    @Column(nullable = false)
    private boolean isActive = true;

    @Column(nullable = false)
    private boolean isPublic = true;

    @Column(length = 500)
    private String location;

    @Column(length = 500)
    private String attachmentUrl;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "created_by")
    private Long createdBy; // Institution admin ID

    @PrePersist
    protected void onCreate() {
        if (eventColor == null) {
            eventColor = "#3b82f6"; // Default blue color
        }
    }
}
