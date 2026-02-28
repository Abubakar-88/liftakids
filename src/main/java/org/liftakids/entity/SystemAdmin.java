package org.liftakids.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "system_admins")
public class SystemAdmin {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long adminId;

    private String name;
    private String email;
    private String username;
    private String password;
    private boolean active = true;

    @OneToMany(mappedBy = "approvedBy", fetch = FetchType.LAZY)
    private List<Institutions> approvedInstitutions;
}