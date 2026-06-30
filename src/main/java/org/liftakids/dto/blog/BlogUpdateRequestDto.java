
package org.liftakids.dto.blog;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BlogUpdateRequestDto {

    @Size(min = 5, max = 200, message = "Title must be between 5 and 200 characters")
    private String title;

    @Size(max = 500, message = "Short description cannot exceed 500 characters")
    private String shortDescription;

    private String content;
    private String featuredImage;
    private String category;
    private String tags;
    private Boolean published;
    private Boolean featured;

    // SEO Fields
    private String slug;

    @Size(max = 160, message = "Meta title cannot exceed 160 characters")
    private String metaTitle;

    @Size(max = 320, message = "Meta description cannot exceed 320 characters")
    private String metaDescription;

    @Size(max = 500, message = "Meta keywords cannot exceed 500 characters")
    private String metaKeywords;

    private String canonicalUrl;
    private Boolean index;
    private Boolean follow;
    private String ogType;
    private String ogImage;
    private String twitterCard;
    private String schemaType;
}
