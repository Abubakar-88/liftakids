package org.liftakids.entity.address;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.liftakids.entity.Institutions;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "unions_or_areas")
public class UnionOrArea {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long unionOrAreaId;
    private String unionOrAreaName;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "thana_id",nullable = false)
    @JsonBackReference
    private Thanas thana;

    @OneToMany(mappedBy = "unionOrArea", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Institutions> institutions = new ArrayList<>();


}
