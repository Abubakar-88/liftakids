package org.liftakids.dto.divison;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.liftakids.dto.district.DistrictResponseDTO;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DivisionResponseDTO {
    private Long divisionId;
    private String divisionName;
    private List<DistrictResponseDTO> districts;
}