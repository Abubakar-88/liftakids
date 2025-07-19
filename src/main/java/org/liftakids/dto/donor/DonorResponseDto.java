package org.liftakids.dto.donor;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.liftakids.entity.DonorType;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DonorResponseDto {
    private Long donorId;
    private String name;
    private String email;
    private String phone;
    private String address;
    private DonorType type;
    private boolean status;
}
