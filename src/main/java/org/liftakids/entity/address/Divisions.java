package org.liftakids.entity.address;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashSet;
import java.util.Set;


@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "divisions")
public class Divisions {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long divisionId;

    @Column(unique = true, nullable = false, length = 15)
    private String divisionName;

    @OneToMany(mappedBy = "division", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<Districts> districts = new HashSet<>();


}
