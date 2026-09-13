package org.liftakids.dto.InstitutionManage.admission;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BulkStudentAdmissionRequest {
    @NotEmpty(message = "Student IDs list cannot be empty")
    private List<Long> studentIds;

    @NotNull(message = "Class ID is required")
    private Long classId;
}
