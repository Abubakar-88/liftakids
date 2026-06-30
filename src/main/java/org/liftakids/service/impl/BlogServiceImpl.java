package org.liftakids.service.impl;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.liftakids.dto.blog.BlogRequestDto;
import org.liftakids.dto.blog.BlogResponseDto;
import org.liftakids.dto.blog.BlogUpdateRequestDto;
import org.liftakids.entity.Blog;
import org.liftakids.entity.SystemAdmin;
import org.liftakids.exception.ResourceNotFoundException;
import org.liftakids.exception.UnauthorizedException;
import org.liftakids.repositories.BlogRepository;
import org.liftakids.repositories.SystemAdminRepository;
import org.liftakids.service.BlogService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class BlogServiceImpl implements BlogService {

    private final BlogRepository blogRepository;
    private final SystemAdminRepository systemAdminRepository;

    public BlogResponseDto convertToDto(Blog blog) {
        if (blog == null) return null;

        return BlogResponseDto.builder()
                .id(blog.getId())
                .title(blog.getTitle())
                .shortDescription(blog.getShortDescription())
                .content(blog.getContent())
                .featuredImage(blog.getFeaturedImage())
                .category(blog.getCategory())
                .tags(blog.getTags())
                .author(blog.getAuthor())
                .adminId(blog.getAdmin() != null ? blog.getAdmin().getAdminId() : null)
                .adminName(blog.getAdmin() != null ? blog.getAdmin().getName() : null)
                .published(blog.isPublished())
                .featured(blog.isFeatured())
                .viewCount(blog.getViewCount())
                .likeCount(blog.getLikeCount())
                .createdAt(blog.getCreatedAt())
                .updatedAt(blog.getUpdatedAt())
                .publishedAt(blog.getPublishedAt())
                // SEO fields
                .slug(blog.getSlug())
                .metaTitle(blog.getMetaTitle())
                .metaDescription(blog.getMetaDescription())
                .metaKeywords(blog.getMetaKeywords())
                .canonicalUrl(blog.getCanonicalUrl())
                .index(blog.isIndex())
                .follow(blog.isFollow())
                .ogType(blog.getOgType())
                .ogImage(blog.getOgImage())
                .twitterCard(blog.getTwitterCard())
                .schemaType(blog.getSchemaType())
                .formattedDate(formatDate(blog.getCreatedAt()))
                .readingTime(calculateReadingTime(blog.getContent()))
                .build();
    }

    private String formatDate(LocalDateTime dateTime) {
        if (dateTime == null) return "";
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM dd, yyyy");
        return dateTime.format(formatter);
    }

    private String calculateReadingTime(String content) {
        if (content == null) return "1 min read";
        int words = content.trim().split("\\s+").length;
        int minutes = (int) Math.ceil(words / 200.0);
        return minutes + " min read";
    }

    @Override
    @Transactional
    public BlogResponseDto createBlog(BlogRequestDto requestDto, Long adminId) {
        log.info("Creating new blog by admin: {}", adminId);

        SystemAdmin admin = systemAdminRepository.findById(adminId)
                .orElseThrow(() -> new ResourceNotFoundException("Admin not found with id: " + adminId));

        // Generate slug if not provided
        String slug = requestDto.getSlug();
        if (slug == null || slug.isEmpty()) {
            slug = generateSlugFromTitle(requestDto.getTitle());
        } else {
            // Check if slug already exists
            if (blogRepository.findBySlug(slug).isPresent()) {
                slug = slug + "-" + System.currentTimeMillis();
            }
        }

        Blog blog = Blog.builder()
                .title(requestDto.getTitle())
                .shortDescription(requestDto.getShortDescription())
                .content(requestDto.getContent())
                .featuredImage(requestDto.getFeaturedImage())
                .category(requestDto.getCategory())
                .tags(requestDto.getTags())
                .author(admin.getName())
                .admin(admin)
                .published(requestDto.isPublished())
                .featured(requestDto.isFeatured())
                .viewCount(0)
                .likeCount(0)
                // SEO fields
                .slug(slug)
                .metaTitle(requestDto.getMetaTitle() != null ? requestDto.getMetaTitle() : requestDto.getTitle())
                .metaDescription(requestDto.getMetaDescription() != null ? requestDto.getMetaDescription() : requestDto.getShortDescription())
                .metaKeywords(requestDto.getMetaKeywords())
                .canonicalUrl(requestDto.getCanonicalUrl())
                .index(requestDto.getIndex() != null ? requestDto.getIndex() : true)
                .follow(requestDto.getFollow() != null ? requestDto.getFollow() : true)
                .ogType(requestDto.getOgType() != null ? requestDto.getOgType() : "article")
                .ogImage(requestDto.getOgImage() != null ? requestDto.getOgImage() : requestDto.getFeaturedImage())
                .twitterCard(requestDto.getTwitterCard() != null ? requestDto.getTwitterCard() : "summary_large_image")
                .schemaType(requestDto.getSchemaType() != null ? requestDto.getSchemaType() : "BlogPosting")
                .build();

        if (blog.isPublished()) {
            blog.setPublishedAt(LocalDateTime.now());
        }

        Blog savedBlog = blogRepository.save(blog);
        log.info("Blog created successfully with id: {}", savedBlog.getId());

        return convertToDto(savedBlog);
    }

    // Helper method to generate slug
    private String generateSlugFromTitle(String title) {
        String slug = title.toLowerCase()
                .replaceAll("[^a-z0-9\\s-]", "")
                .replaceAll("\\s+", "-")
                .replaceAll("-+", "-")
                .trim();

        // Check if slug exists, append number if needed
        String originalSlug = slug;
        int counter = 1;
        while (blogRepository.findBySlug(slug).isPresent()) {
            slug = originalSlug + "-" + counter;
            counter++;
        }

        return slug;
    }

    @Override
    public BlogResponseDto getBlogById(Long id) {
        Blog blog = blogRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Blog not found with id: " + id));
        return convertToDto(blog);
    }

    @Override
    public BlogResponseDto getPublishedBlogById(Long id) {
        Blog blog = blogRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Blog not found with id: " + id));

        if (!blog.isPublished()) {
            throw new ResourceNotFoundException("Blog not found");
        }

        return convertToDto(blog);
    }

    @Override
    public Page<BlogResponseDto> getAllBlogs(Pageable pageable) {
        return blogRepository.findAll(pageable).map(this::convertToDto);
    }

    @Override
    public Page<BlogResponseDto> getAllPublishedBlogs(Pageable pageable) {
        return blogRepository.findByPublishedTrue(pageable).map(this::convertToDto);
    }

    @Override
    public Page<BlogResponseDto> getFeaturedBlogs(Pageable pageable) {
        // Corrected: Single pageable parameter
        return blogRepository.findByPublishedTrueAndFeaturedTrue(pageable)
                .map(this::convertToDto);
    }

    @Override
    public Page<BlogResponseDto> getBlogsByCategory(String category, Pageable pageable) {
        return blogRepository.findByPublishedTrueAndCategory(category, pageable).map(this::convertToDto);
    }

    @Override
    public Page<BlogResponseDto> searchBlogs(String keyword, Pageable pageable) {
        return blogRepository.searchPublishedBlogs(keyword, pageable).map(this::convertToDto);
    }

    @Override
    public List<String> getAllCategories() {
        return blogRepository.findAllCategories();
    }

    @Override
    public Page<BlogResponseDto> getLatestBlogs(Pageable pageable) {
        // ✅ Corrected: Use correct repository method name
        return blogRepository.findLatestPublishedBlogs(pageable)
                .map(this::convertToDto);
    }

    @Override
    public Page<BlogResponseDto> getMostViewedBlogs(Pageable pageable) {
        // ✅ Corrected: Use correct repository method name
        return blogRepository.findMostViewedPublishedBlogs(pageable)
                .map(this::convertToDto);
    }

    @Override
    @Transactional
    public BlogResponseDto updateBlog(Long id, BlogUpdateRequestDto requestDto, Long adminId) {
        log.info("Updating blog: {} by admin: {}", id, adminId);

        Blog blog = blogRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Blog not found with id: " + id));

        // Check admin permission
        if (!blog.getAdmin().getAdminId().equals(adminId)) {
            SystemAdmin admin = systemAdminRepository.findById(adminId)
                    .orElseThrow(() -> new ResourceNotFoundException("Admin not found"));

            if (!"SUPER_ADMIN".equals(admin.getUsername())) {
                throw new UnauthorizedException("You are not authorized to update this blog");
            }
        }

        if (requestDto.getTitle() != null) {
            blog.setTitle(requestDto.getTitle());
        }
        if (requestDto.getShortDescription() != null) {
            blog.setShortDescription(requestDto.getShortDescription());
        }
        if (requestDto.getContent() != null) {
            blog.setContent(requestDto.getContent());
        }
        if (requestDto.getFeaturedImage() != null) {
            blog.setFeaturedImage(requestDto.getFeaturedImage());
        }
        if (requestDto.getCategory() != null) {
            blog.setCategory(requestDto.getCategory());
        }
        if (requestDto.getTags() != null) {
            blog.setTags(requestDto.getTags());
        }
        if (requestDto.getPublished() != null) {
            boolean wasPublished = blog.isPublished();
            blog.setPublished(requestDto.getPublished());
            if (!wasPublished && blog.isPublished()) {
                blog.setPublishedAt(LocalDateTime.now());
            }
        }
        if (requestDto.getFeatured() != null) {
            blog.setFeatured(requestDto.getFeatured());
        }

        Blog updatedBlog = blogRepository.save(blog);
        log.info("Blog updated successfully: {}", updatedBlog.getId());

        return convertToDto(updatedBlog);
    }

    @Override
    @Transactional
    public void deleteBlog(Long id, Long adminId) {
        log.info("Deleting blog: {} by admin: {}", id, adminId);

        Blog blog = blogRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Blog not found with id: " + id));

        // Check admin permission
        if (!blog.getAdmin().getAdminId().equals(adminId)) {
            SystemAdmin admin = systemAdminRepository.findById(adminId)
                    .orElseThrow(() -> new ResourceNotFoundException("Admin not found"));

            if (!"SUPER_ADMIN".equals(admin.getUsername())) {
                throw new UnauthorizedException("You are not authorized to delete this blog");
            }
        }

        blogRepository.delete(blog);
        log.info("Blog deleted successfully: {}", id);
    }

    @Override
    @Transactional
    public void incrementViewCount(Long id) {
        blogRepository.findById(id).ifPresent(blog -> {
            blog.setViewCount(blog.getViewCount() + 1);
            blogRepository.save(blog);
        });
    }

    @Override
    @Transactional
    public void incrementLikeCount(Long id) {
        blogRepository.findById(id).ifPresent(blog -> {
            blog.setLikeCount(blog.getLikeCount() + 1);
            blogRepository.save(blog);
        });
    }

    @Override
    public long getTotalPublishedBlogs() {
        return blogRepository.countPublishedBlogs();
    }

    @Override
    public Page<BlogResponseDto> getAllBlogsAdmin(Pageable pageable) {
        return blogRepository.findAll(pageable).map(this::convertToDto);
    }


    @Override
    @Transactional()
    public BlogResponseDto getBlogBySlug(String slug) {
        log.info("Fetching blog by slug: {}", slug);

        Blog blog = blogRepository.findBySlug(slug)
                .orElseThrow(() -> new ResourceNotFoundException("Blog not found with slug: " + slug));

        return convertToDto(blog);
    }

    @Override
    @Transactional
    public BlogResponseDto getPublishedBlogBySlug(String slug) {
        log.info("Fetching published blog by slug: {}", slug);

        Blog blog = blogRepository.findBySlugAndPublishedTrue(slug)
                .orElseThrow(() -> new ResourceNotFoundException("Blog not found with slug: " + slug));

        // Increment view count
        blog.setViewCount(blog.getViewCount() + 1);
        blogRepository.save(blog);

        log.info("Blog view count incremented for slug: {}, new count: {}", slug, blog.getViewCount());

        return convertToDto(blog);
    }
    @Override
    public Page<BlogResponseDto> getAllPublishedBlogsForSitemap(Pageable pageable) {
        return blogRepository.findByPublishedTrue(pageable).map(this::convertToDto);
    }

    @Override
    public long getTotalPublishedBlogsCount() {
        return blogRepository.countPublishedBlogs();
    }


}
