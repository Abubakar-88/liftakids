package org.liftakids.entity.address;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.liftakids.entity.Institutions;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "villege_or_house_no")
public class VillegeOrHouseNo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;

    @ManyToOne
    @JoinColumn(name = "union_or_area_id")
    @JsonBackReference
    private UnionOrArea unionOrArea;

    @OneToMany(mappedBy = "villegeOrHouseNo", cascade = CascadeType.ALL)
    private List<Institutions> institutions;

}
