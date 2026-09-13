package org.liftakids.dto.teacher;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NoticeDto {
    private Long id;
    private String title;
    private String content;
    private String priority;
    private LocalDateTime createdAt;
    private LocalDateTime expiresAt;
    private boolean isRead;
}