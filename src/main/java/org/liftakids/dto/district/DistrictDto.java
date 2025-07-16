package org.liftakids.dto.district;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DistrictDto {
    private Long districtId;
    private String districtName;
    private Long divisionId;
}
