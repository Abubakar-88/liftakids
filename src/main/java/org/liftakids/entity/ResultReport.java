package org.liftakids.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class ResultReport {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;
    private String reportFileUrl;
    private LocalDate uploadDate;

    @ManyToOne
    @JoinColumn(name = "student_id")
    private Student student;


}
