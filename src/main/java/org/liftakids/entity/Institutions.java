package org.liftakids.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.liftakids.entity.address.*;

import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class Institutions {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String type; // kowmi, alia, school,
    private String email;
    private String phone;
    private String registrationNumber;
    private String addressDetails; // Specific address details

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "villege_or_house_no_id", nullable = false)
    private VillegeOrHouseNo villegeOrHouseNo;
    
    
    private LocalDateTime updateDate;
    
    private String password;
    private boolean approved = false;

    private LocalDateTime registrationDate = LocalDateTime.now();

    @OneToMany(mappedBy = "institution", cascade = CascadeType.ALL)
    private List<Student> students;

}
