package org.liftakids.repositories;

import org.liftakids.entity.Blog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface BlogRepository extends JpaRepository<Blog, Long> {
    // ========== BASIC QUERIES ==========

    // Find by slug
    Optional<Blog> findBySlug(String slug);

    Optional<Blog> findBySlugAndPublishedTrue(String slug);

    boolean existsBySlug(String slug);

    // ========== PUBLISHED BLOGS ==========

    // Get all published blogs with pagination
    Page<Blog> findByPublishedTrue(Pageable pageable);

    // Get all published blogs without pagination (for sitemap)
    List<Blog> findByPublishedTrue();

    // Get published blogs ordered by updated date descending
    List<Blog> findByPublishedTrueOrderByUpdatedAtDesc();

    // Get published blogs ordered by updated date descending with pagination
    Page<Blog> findByPublishedTrueOrderByUpdatedAtDesc(Pageable pageable);

    // Get published blogs ordered by created date descending
    List<Blog> findByPublishedTrueOrderByCreatedAtDesc();

    Page<Blog> findByPublishedTrueOrderByCreatedAtDesc(Pageable pageable);

    // ========== FEATURED BLOGS ==========

    // Get featured and published blogs
    // ========== FEATURED BLOGS ==========
    Page<Blog> findByPublishedTrueAndFeaturedTrue(Pageable pageable);

    // ========== LATEST BLOGS ==========
//    @Query("SELECT b FROM Blog b WHERE b.published = true ORDER BY b.createdAt DESC")
//    Page<Blog> findLatestPublishedBlogs(Pageable pageable);

    // ========== MOST VIEWED BLOGS ==========
    @Query("SELECT b FROM Blog b WHERE b.published = true ORDER BY b.viewCount DESC")
    Page<Blog> findMostViewedPublishedBlogs(Pageable pageable);
    List<Blog> findByPublishedTrueAndFeaturedTrue();

    // ========== CATEGORY BASED ==========

    // Get blogs by category
    Page<Blog> findByPublishedTrueAndCategory(String category, Pageable pageable);

    List<Blog> findByPublishedTrueAndCategory(String category);

    // Get all distinct categories
    @Query("SELECT DISTINCT b.category FROM Blog b WHERE b.published = true AND b.category IS NOT NULL AND b.category != ''")
    List<String> findAllCategories();

    // ========== SEARCH QUERIES ==========

    // Search in published blogs
    @Query("SELECT b FROM Blog b WHERE b.published = true AND " +
            "(LOWER(b.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(b.content) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(b.tags) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(b.shortDescription) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<Blog> searchPublishedBlogs(@Param("keyword") String keyword, Pageable pageable);

    // Search in all blogs (admin)
    @Query("SELECT b FROM Blog b WHERE " +
            "LOWER(b.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(b.content) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(b.author) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<Blog> searchAllBlogs(@Param("keyword") String keyword, Pageable pageable);

    // ========== ORDER BY VIEWS/LIKES ==========

    // Most viewed published blogs
    Page<Blog> findByPublishedTrueOrderByViewCountDesc(Pageable pageable);

    List<Blog> findByPublishedTrueOrderByViewCountDesc();

    // Most liked published blogs
    Page<Blog> findByPublishedTrueOrderByLikeCountDesc(Pageable pageable);

    // ========== LATEST BLOGS ==========

    // Latest published blogs
    @Query("SELECT b FROM Blog b WHERE b.published = true ORDER BY b.createdAt DESC")
    Page<Blog> findLatestPublishedBlogs(Pageable pageable);

    @Query("SELECT b FROM Blog b WHERE b.published = true ORDER BY b.createdAt DESC")
    List<Blog> findLatestPublishedBlogs();

    // ========== DATE RANGE QUERIES ==========

    // Get blogs published between dates
    @Query("SELECT b FROM Blog b WHERE b.published = true AND b.publishedAt BETWEEN :startDate AND :endDate")
    List<Blog> findBlogsByDateRange(@Param("startDate") LocalDateTime startDate,
                                    @Param("endDate") LocalDateTime endDate);

    // Get blogs by month and year
    @Query("SELECT b FROM Blog b WHERE b.published = true AND " +
            "YEAR(b.publishedAt) = :year AND MONTH(b.publishedAt) = :month")
    List<Blog> findBlogsByMonth(@Param("year") int year, @Param("month") int month);

    // ========== COUNT QUERIES ==========

    // Count published blogs
    @Query("SELECT COUNT(b) FROM Blog b WHERE b.published = true")
    long countPublishedBlogs();

    // Count blogs by category
    @Query("SELECT b.category, COUNT(b) FROM Blog b WHERE b.published = true GROUP BY b.category")
    List<Object[]> countBlogsByCategory();

    // Count blogs by author
    @Query("SELECT b.author, COUNT(b) FROM Blog b GROUP BY b.author")
    List<Object[]> countBlogsByAuthor();

    // ========== ADMIN QUERIES ==========

    // Get all blogs ordered by creation date (admin)
    Page<Blog> findAllByOrderByCreatedAtDesc(Pageable pageable);

    // Get blogs by admin
    List<Blog> findByAdmin_AdminId(Long adminId);

    Page<Blog> findByAdmin_AdminId(Long adminId, Pageable pageable);

    // Get unpublished blogs
    List<Blog> findByPublishedFalse();

    Page<Blog> findByPublishedFalse(Pageable pageable);

    // ========== SITEMAP SPECIFIC ==========

    // Get all published blogs for sitemap (ordered by updated date)
    @Query("SELECT b FROM Blog b WHERE b.published = true ORDER BY b.updatedAt DESC")
    List<Blog> findAllPublishedForSitemap();

    // Get paginated published blogs for sitemap
    @Query("SELECT b FROM Blog b WHERE b.published = true ORDER BY b.updatedAt DESC")
    Page<Blog> findPaginatedPublishedForSitemap(Pageable pageable);

    // Get recent blogs for sitemap (last 30 days)
    @Query("SELECT b FROM Blog b WHERE b.published = true AND b.updatedAt >= :since ORDER BY b.updatedAt DESC")
    List<Blog> findRecentPublishedBlogs(@Param("since") LocalDateTime since);
}
