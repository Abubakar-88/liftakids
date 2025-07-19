package org.liftakids.dto.resultReport;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class ResultUploadRequestDto {
    private Long studentId;
    private String terminal; // First Terminal or Second Terminal
    private MultipartFile file;
}
