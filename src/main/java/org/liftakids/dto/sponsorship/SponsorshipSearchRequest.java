package org.liftakids.dto.sponsorship;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.liftakids.entity.SponsorshipStatus;
import org.springframework.data.domain.Sort;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SponsorshipSearchRequest {
    private Integer page = 0;
    private Integer size = 10;
    private String sortBy = "startDate";
    private String sortDirection = "desc";

    // Search criteria
    private Long sponsorId;
    private String studentName;
    private String donorName;
    private String institutionName;
    private Boolean overdueOnly;
    private String status;
    private String paymentMethod;
    private LocalDate startDate;
    private LocalDate endDate;

    public Sort.Direction getSortDirection() {
        return "desc".equalsIgnoreCase(sortDirection) ? Sort.Direction.DESC : Sort.Direction.ASC;
    }
}
