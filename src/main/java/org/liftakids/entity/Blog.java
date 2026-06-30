package org.liftakids.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "blogs")
public class Blog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(nullable = false, length = 500)
    private String shortDescription;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(length = 500)
    private String featuredImage;

    @Column(length = 100)
    private String category;

    @Column(length = 500)
    private String tags;

    @Column(nullable = false)
    private String author;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "admin_id")
    private SystemAdmin admin;

    @Column(nullable = false)
    private boolean published = true;

    @Column(nullable = false)
    private boolean featured = false;

    @Column(nullable = false)
    private int viewCount = 0;

    @Column(nullable = false)
    private int likeCount = 0;

    // ========== SEO FIELDS ==========

    @Column(unique = true, length = 200)
    private String slug;  // URL-friendly title (e.g., "how-to-help-children")

    @Column(length = 160)
    private String metaTitle;  // SEO title (max 60-70 chars)

    @Column(length = 320)
    private String metaDescription;  // SEO description (max 150-160 chars)

    @Column(length = 500)
    private String metaKeywords;  // Comma separated keywords

    @Column(length = 500)
    private String canonicalUrl;  // Canonical URL for duplicate content

    @Column(name = "is_indexed", nullable = false)
    private boolean index = true;  // Allow search engines to index

    @Column(nullable = false)
    private boolean follow = true;  // Allow search engines to follow links

    @Column(length = 50)
    private String ogType = "article";  // Open Graph type

    @Column(length = 500)
    private String ogImage;  // Open Graph image

    @Column(length = 100)
    private String twitterCard = "summary_large_image";  // Twitter card type

    @Column(length = 100)
    private String schemaType = "BlogPosting";  // Schema.org type

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "published_at")
    private LocalDateTime publishedAt;

    @Column(name = "last_modified_at")
    private LocalDateTime lastModifiedAt;

    @PrePersist
    protected void onCreate() {
        if (publishedAt == null && published) {
            publishedAt = LocalDateTime.now();
        }
        if (slug == null && title != null) {
            generateSlug();
        }
        if (metaTitle == null && title != null) {
            metaTitle = title;
        }
        if (metaDescription == null && shortDescription != null) {
            metaDescription = shortDescription;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        if (published && publishedAt == null) {
            publishedAt = LocalDateTime.now();
        }
        lastModifiedAt = LocalDateTime.now();
        if (slug == null && title != null) {
            generateSlug();
        }
    }

    private void generateSlug() {
        if (title != null) {
            this.slug = title.toLowerCase()
                    .replaceAll("[^a-z0-9\\s-]", "")
                    .replaceAll("\\s+", "-")
                    .replaceAll("-+", "-")
                    .trim();
        }
    }
}
