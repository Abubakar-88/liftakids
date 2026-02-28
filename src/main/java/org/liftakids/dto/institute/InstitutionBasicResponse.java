package org.liftakids.dto.institute;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.liftakids.dto.district.DistrictResponseDTO;
import org.liftakids.dto.divison.DivisionResponseDTO;
import org.liftakids.dto.student.StudentResponseDto;
import org.liftakids.dto.thana.ThanaResponseDTO;
import org.liftakids.dto.unionOrArea.UnionOrAreaResponseDTO;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class InstitutionBasicResponse extends InstitutionResponseDto {

    private DivisionResponseDTO division;
    private DistrictResponseDTO district;
    private ThanaResponseDTO thana;
    private UnionOrAreaResponseDTO unionOrArea;
    List<StudentResponseDto> students;

}
