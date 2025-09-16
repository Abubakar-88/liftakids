package org.liftakids.dto.donor;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.liftakids.entity.Donor;
import org.liftakids.entity.DonorType;

@Data
@Builder
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
    private int sponsoredStudentsCount;
    private int totalSponsorshipsCount;
    public static DonorResponseDto fromEntity(Donor donor) {
        return DonorResponseDto.builder()
                .donorId(donor.getDonorId())
                .name(donor.getName())
                .email(donor.getEmail())
                .phone(donor.getPhone())
                .address(donor.getAddress())
                .type(donor.getType())
                .status(donor.isStatus())
                .sponsoredStudentsCount(donor.getActiveSponsorships().size())
                .totalSponsorshipsCount(donor.getSponsorships().size())
                .build();
    }


}
