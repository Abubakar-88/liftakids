package org.liftakids.dto.institute;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.liftakids.dto.district.DistrictResponseDTO;
import org.liftakids.dto.divison.DivisionResponseDTO;
import org.liftakids.dto.student.StudentResponseDto;
import org.liftakids.dto.thana.ThanaResponseDTO;
import org.liftakids.dto.unionOrArea.UnionOrAreaResponseDTO;
import org.liftakids.entity.InstitutionType;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class InstitutionBasicResponse {
    private Long institutionsId;
    private String institutionName;
    private DivisionResponseDTO division;
    private DistrictResponseDTO district;
    private ThanaResponseDTO thana;
    private UnionOrAreaResponseDTO unionOrArea;
    private String villageOrHouse;
    private InstitutionType type;
    private String email;
    private String phone;
    private Boolean approved;
    List<StudentResponseDto> students;

}
