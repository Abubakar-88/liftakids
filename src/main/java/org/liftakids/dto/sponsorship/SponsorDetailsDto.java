package org.liftakids.dto.sponsorship;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SponsorDetailsDto {
    private Long donorId;
    private String name;
    private String email;
    private String phone;
    private String address;
}
