package org.liftakids.dto.pages;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class StaticPageResponseDTO {
    private Long id;
    private String slug;
    private String title;
    private String content;
    private String metaTitle;
    private String metaDescription;
    private String metaKeywords;
    private boolean published;
    private int sortOrder;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
