package org.liftakids.dto.unionOrArea;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UnionOrAreaResponseDTO {
    private Long unionOrAreaId;
    private String unionOrAreaName;
    private String thanaName;

    // Add these for full hierarchy in response
    private Long thanaId;
    private Long districtId;
    private String districtName;
    private Long divisionId;
    private String divisionName;
}
