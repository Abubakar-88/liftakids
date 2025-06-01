package org.liftakids.dto;


import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class InstitutionResponseDTO {
    private Long id;
    private String name;
    private String email;
    private String phone;
    private boolean approved;
    private String division;
    private String district;
    private String thana;
    private String union;

}