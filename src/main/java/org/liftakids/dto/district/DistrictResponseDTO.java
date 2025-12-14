package org.liftakids.dto.district;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.liftakids.dto.thana.ThanaResponseDTO;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DistrictResponseDTO {
    private Long districtId;
    private String districtName;
    private Long divisionId;
    private String divisionName;

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private Set<ThanaResponseDTO> thanas = new HashSet<>();
}
