// BlogResponseDto.java - SEO fields যোগ করুন
package org.liftakids.dto.blog;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BlogResponseDto {
    private Long id;
    private String title;
    private String shortDescription;
    private String content;
    private String featuredImage;
    private String category;
    private String tags;
    private String author;
    private Long adminId;
    private String adminName;
    private boolean published;
    private boolean featured;
    private int viewCount;
    private int likeCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime publishedAt;
    private String formattedDate;
    private String readingTime;

    // SEO Fields
    private String slug;
    private String metaTitle;
    private String metaDescription;
    private String metaKeywords;
    private String canonicalUrl;
    private boolean index;
    private boolean follow;
    private String ogType;
    private String ogImage;
    private String twitterCard;
    private String schemaType;

    // SEO Helper Methods
    private String getFullMetaTitle() {
        return metaTitle != null ? metaTitle : title;
    }

    private String getFullMetaDescription() {
        return metaDescription != null ? metaDescription : shortDescription;
    }

    private String getFullCanonicalUrl() {
        return canonicalUrl != null ? canonicalUrl : "/blog/" + slug;
    }
}