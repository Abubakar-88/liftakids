package org.liftakids.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Where;

import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "donors")
public class Donor {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String email;
    private String phone;
    private String address;

    @Enumerated(EnumType.STRING)
    private DonorType type; // INDIVIDUAL, ORGANIZATION

    @OneToMany(mappedBy = "donor", cascade = CascadeType.ALL, orphanRemoval = true)
    @Where(clause = "status = 'ACTIVE'")
    private List<Sponsorship> activeSponsorships = new ArrayList<>();

    public boolean canSponsorMoreStudents(int maxAllowed) {
        return activeSponsorships.size() < maxAllowed;
    }



}
