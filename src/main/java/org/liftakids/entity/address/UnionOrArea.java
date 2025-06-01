package org.liftakids.entity.address;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "unions_or_areas")
public class UnionOrArea {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;

    @ManyToOne
    @JoinColumn(name = "thana_id")
    @JsonBackReference
    private Thanas thanas;

    @OneToMany(mappedBy = "unionOrArea", cascade = CascadeType.ALL)
    private List<VillegeOrHouseNo> villegeOrHouseNos;

}
