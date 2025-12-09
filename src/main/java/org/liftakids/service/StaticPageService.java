package org.liftakids.service;

import org.liftakids.entity.StaticPage;

import java.util.List;
import java.util.Optional;

public interface StaticPageService {
    // Get page by slug (for public)
    Optional<StaticPage> getPageBySlug(String slug);

    // Get all pages (for admin)
    List<StaticPage> getAllPages();

    // Get page by ID
    Optional<StaticPage> getPageById(Long id);

    // Create or update page
    StaticPage savePage(StaticPage page);

    // Delete page
    void deletePage(Long id);

    // Check if slug exists
    boolean slugExists(String slug);

    // Get published pages only
    List<StaticPage> getPublishedPages();
}
