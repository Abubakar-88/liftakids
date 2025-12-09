package org.liftakids.dto.pages;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StaticPageRequestDTO {
    private Long id;
    @NotBlank(message = "Slug is required")
    private String slug;

    @NotBlank(message = "Title is required")
    private String title;

    @NotBlank(message = "Content is required")
    private String content;

    private String metaTitle;
    private String metaDescription;
    private String metaKeywords;
    private boolean published;
    private int sortOrder;
}
