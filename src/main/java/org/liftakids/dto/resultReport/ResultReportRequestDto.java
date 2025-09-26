package org.liftakids.dto.resultReport;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class ResultReportRequestDto {
    private Long studentId;
    private MultipartFile resultImage;
    private Boolean published = false;
}