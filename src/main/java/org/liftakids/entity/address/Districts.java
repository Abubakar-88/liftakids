package org.liftakids.entity.address;

import com.fasterxml.jackson.annotation.JsonIgnore;
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
@Table(name = "districts")
public class Districts {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long districtId;
    @Column(unique = true, nullable = false, length = 15)
    private String districtName;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "division_id", nullable = false)
    private Divisions division;

    @OneToMany(mappedBy = "district", cascade = CascadeType.ALL,fetch = FetchType.LAZY)
    @JsonIgnore
    private Set<Thanas> thanas = new HashSet<>();


}
