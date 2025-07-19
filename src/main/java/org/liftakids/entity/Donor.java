package org.liftakids.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
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
    private Long donorId;
    @NotBlank(message = "Name is required")
    private String name;
    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;
    @NotBlank(message = "Phone number is required")
    @Pattern(
            regexp = "^(\\+\\d{1,3}[- ]?)?\\d{7,15}$",
            message = "Invalid international phone number"
    )
    private String phone;
    @NotBlank(message = "Address is required")
    private String address;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DonorType type; // INDIVIDUAL, ORGANIZATION
    private boolean status = true;

    @OneToMany(mappedBy = "donor", cascade = CascadeType.ALL, orphanRemoval = true)
    @Where(clause = "status = 'ACTIVE'")
    private List<Sponsorship> activeSponsorships = new ArrayList<>();

    public boolean canSponsorMoreStudents(int maxAllowed) {
        return activeSponsorships.size() < maxAllowed;
    }



}
