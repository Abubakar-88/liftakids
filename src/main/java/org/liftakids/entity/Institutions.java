package org.liftakids.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.liftakids.entity.address.UnionOrArea;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Institutions {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long institutionsId;

    @Column(name = "institution_name", nullable = false)
    private String institutionName;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "union_or_area_id", nullable = false)
    private UnionOrArea unionOrArea;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private InstitutionType type;

    @Email
    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false, length = 15)
    private String phone;

    @Column(name = "village_or_house")
    private String villageOrHouse;

    @Column(nullable = false)
    private String password;

    @Column(name = "registration_date")
    private LocalDateTime registrationDate;

    private Boolean approved;

    private LocalDateTime updateDate;

    @OneToMany(mappedBy = "institution", cascade = CascadeType.ALL)
    private List<Student> students;

}
