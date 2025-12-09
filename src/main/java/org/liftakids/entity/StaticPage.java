package org.liftakids.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class StaticPage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String slug; // about-us, contact, mission, etc.
    private String title;

    @Column(columnDefinition = "TEXT")
    private String content;
    @Column(name = "is_contact_page")
    private boolean contactPage = false;
    private String metaTitle;
    private String metaDescription;
    private String metaKeywords;
    private boolean published;
    private int sortOrder;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
