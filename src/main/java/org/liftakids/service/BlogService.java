package org.liftakids.service;

import org.liftakids.dto.blog.BlogRequestDto;
import org.liftakids.dto.blog.BlogResponseDto;
import org.liftakids.dto.blog.BlogUpdateRequestDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface BlogService {
    BlogResponseDto createBlog(BlogRequestDto requestDto, Long adminId);
    // Read
    BlogResponseDto getBlogById(Long id);

    BlogResponseDto getPublishedBlogById(Long id);

    Page<BlogResponseDto> getAllBlogs(Pageable pageable);

    Page<BlogResponseDto> getAllPublishedBlogs(Pageable pageable);

    Page<BlogResponseDto> getFeaturedBlogs(Pageable pageable);

    Page<BlogResponseDto> getBlogsByCategory(String category, Pageable pageable);

    Page<BlogResponseDto> searchBlogs(String keyword, Pageable pageable);

    List<String> getAllCategories();

    Page<BlogResponseDto> getLatestBlogs(Pageable pageable);

    Page<BlogResponseDto> getMostViewedBlogs(Pageable pageable);

    // Update
    BlogResponseDto updateBlog(Long id, BlogUpdateRequestDto requestDto, Long adminId);

    // Delete
    void deleteBlog(Long id, Long adminId);

    // Actions
    void incrementViewCount(Long id);

    void incrementLikeCount(Long id);

    // Stats
    long getTotalPublishedBlogs();

    // Admin specific
    Page<BlogResponseDto> getAllBlogsAdmin(Pageable pageable);


    BlogResponseDto getBlogBySlug(String slug);

    BlogResponseDto getPublishedBlogBySlug(String slug);

    Page<BlogResponseDto> getAllPublishedBlogsForSitemap(Pageable pageable);
    long getTotalPublishedBlogsCount();
}
