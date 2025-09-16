package org.liftakids.dto.thana;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.liftakids.dto.unionOrArea.UnionOrAreaResponseDTO;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ThanaResponseDTO {
    private Long thanaId;
    private String thanaName;
    private Long districtId;
    private String districtName;
    private Long divisionId;
    private String divisionName;
    private List<UnionOrAreaResponseDTO> unionOrAreas;
}