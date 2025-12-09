package org.liftakids.repositories;

import org.liftakids.entity.StaticPage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface StaticPageRepository extends JpaRepository<StaticPage, Long> {
    // Find by slug and published status
    Optional<StaticPage> findBySlugAndPublished(String slug, boolean published);

    // Find all ordered by sort order
    List<StaticPage> findAllByOrderBySortOrderAsc();

    // Find published pages ordered by sort order
    List<StaticPage> findByPublishedTrueOrderBySortOrderAsc();

    // Check if slug exists
    boolean existsBySlug(String slug);

    // Find by slug (for admin - without published check)
    Optional<StaticPage> findBySlug(String slug);
}
