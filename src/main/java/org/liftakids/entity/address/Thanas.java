package org.liftakids.entity.address;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "thanas")
public class Thanas {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long thanaId;
    private String thanaName;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "district_id", nullable = false)
    @JsonIgnore
    private Districts district;

    @OneToMany(mappedBy = "thana", cascade = CascadeType.ALL)
    private List<UnionOrArea> unionOrAreas;



}