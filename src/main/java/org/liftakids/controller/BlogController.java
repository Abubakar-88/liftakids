package org.liftakids.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.liftakids.dto.blog.BlogRequestDto;
import org.liftakids.dto.blog.BlogResponseDto;
import org.liftakids.dto.blog.BlogUpdateRequestDto;
import org.liftakids.entity.Blog;
import org.liftakids.repositories.BlogRepository;
import org.liftakids.service.BlogService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.zip.GZIPOutputStream;

@Slf4j
@RestController
@RequestMapping("/api/blogs")
@RequiredArgsConstructor
public class BlogController {

    private final BlogService blogService;
    private final BlogRepository blogRepository;

    // ============= ADMIN ENDPOINTS =============

    @PostMapping("/admin/create")
    public ResponseEntity<BlogResponseDto> createBlog(
            @Valid @RequestBody BlogRequestDto requestDto,
            @RequestParam Long adminId) {
        log.info("Creating blog by admin: {}", adminId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(blogService.createBlog(requestDto, adminId));
    }

    @PutMapping("/admin/{id}")
    public ResponseEntity<BlogResponseDto> updateBlog(
            @PathVariable Long id,
            @Valid @RequestBody BlogUpdateRequestDto requestDto,
            @RequestParam Long adminId) {
        log.info("Updating blog: {} by admin: {}", id, adminId);
        return ResponseEntity.ok(blogService.updateBlog(id, requestDto, adminId));
    }

    @DeleteMapping("/admin/{id}")
    public ResponseEntity<Map<String, String>> deleteBlog(
            @PathVariable Long id,
            @RequestParam Long adminId) {
        log.info("Deleting blog: {} by admin: {}", id, adminId);
        blogService.deleteBlog(id, adminId);
        Map<String, String> response = new HashMap<>();
        response.put("message", "Blog deleted successfully");
        return ResponseEntity.ok(response);
    }

    @GetMapping("/admin/all")
    public ResponseEntity<Page<BlogResponseDto>> getAllBlogsAdmin(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {

        Sort sort = sortDir.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);

        return ResponseEntity.ok(blogService.getAllBlogsAdmin(pageable));
    }

    @GetMapping("/admin/slug/{slug}")
    public ResponseEntity<BlogResponseDto> getBlogBySlug(@PathVariable String slug) {
        log.info("API: Fetching blog by slug: {} (Admin access)", slug);
        BlogResponseDto blog = blogService.getBlogBySlug(slug);
        return ResponseEntity.ok(blog);
    }

    // ============= PUBLIC ENDPOINTS =============

    @GetMapping("/public")
    public ResponseEntity<Page<BlogResponseDto>> getAllPublishedBlogs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {

        Sort sort = sortDir.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);

        return ResponseEntity.ok(blogService.getAllPublishedBlogs(pageable));
    }

    @GetMapping("/public/featured")
    public ResponseEntity<Page<BlogResponseDto>> getFeaturedBlogs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "6") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(blogService.getFeaturedBlogs(pageable));
    }

    @GetMapping("/public/latest")
    public ResponseEntity<Page<BlogResponseDto>> getLatestBlogs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(blogService.getLatestBlogs(pageable));
    }

    @GetMapping("/public/most-viewed")
    public ResponseEntity<Page<BlogResponseDto>> getMostViewedBlogs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(blogService.getMostViewedBlogs(pageable));
    }

    @GetMapping("/public/category/{category}")
    public ResponseEntity<Page<BlogResponseDto>> getBlogsByCategory(
            @PathVariable String category,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(blogService.getBlogsByCategory(category, pageable));
    }

    @GetMapping("/public/categories")
    public ResponseEntity<List<String>> getAllCategories() {
        return ResponseEntity.ok(blogService.getAllCategories());
    }

    @GetMapping("/public/search")
    public ResponseEntity<Page<BlogResponseDto>> searchBlogs(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(blogService.searchBlogs(keyword, pageable));
    }

    @GetMapping("/public/{id}")
    public ResponseEntity<BlogResponseDto> getPublishedBlogById(@PathVariable Long id) {
        BlogResponseDto blog = blogService.getPublishedBlogById(id);
        blogService.incrementViewCount(id);
        return ResponseEntity.ok(blog);
    }

    @GetMapping("/public/slug/{slug}")
    public ResponseEntity<BlogResponseDto> getPublishedBlogBySlug(@PathVariable String slug) {
        log.info("API: Fetching published blog by slug: {}", slug);
        BlogResponseDto blog = blogService.getPublishedBlogBySlug(slug);
        blogService.incrementViewCount(blog.getId());
        return ResponseEntity.ok(blog);
    }

    @PostMapping("/public/{id}/like")
    public ResponseEntity<Map<String, Integer>> likeBlog(@PathVariable Long id) {
        blogService.incrementLikeCount(id);
        BlogResponseDto blog = blogService.getPublishedBlogById(id);
        Map<String, Integer> response = new HashMap<>();
        response.put("likes", blog.getLikeCount());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/public/stats")
    public ResponseEntity<Map<String, Long>> getBlogStats() {
        Map<String, Long> stats = new HashMap<>();
        stats.put("totalBlogs", blogService.getTotalPublishedBlogs());
        return ResponseEntity.ok(stats);
    }

    // ============= SEO META TAGS ENDPOINTS =============

    @GetMapping("/public/{id}/seo-meta")
    public ResponseEntity<Map<String, String>> getBlogSeoMeta(@PathVariable Long id) {
        BlogResponseDto blog = blogService.getPublishedBlogById(id);
        return ResponseEntity.ok(generateSeoMeta(blog));
    }

    @GetMapping("/public/slug/{slug}/seo-meta")
    public ResponseEntity<Map<String, String>> getBlogSeoMetaBySlug(@PathVariable String slug) {
        BlogResponseDto blog = blogService.getPublishedBlogBySlug(slug);
        return ResponseEntity.ok(generateSeoMeta(blog));
    }

    // ============= SITEMAP ENDPOINTS =============

    @GetMapping(value = "/public/sitemap.xml", produces = "application/xml")
    public ResponseEntity<String> getSitemapXml() {
        log.info("Generating XML sitemap");

        List<Blog> blogs = blogRepository.findByPublishedTrueOrderByUpdatedAtDesc();

        StringBuilder xml = new StringBuilder();
        xml.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        xml.append("<urlset xmlns=\"http://www.sitemaps.org/schemas/sitemap/0.9\">\n");

        // Add static pages
        addUrlToSitemap(xml, "/", "daily", "1.0");
        addUrlToSitemap(xml, "/about", "monthly", "0.8");
        addUrlToSitemap(xml, "/contact", "monthly", "0.8");
        addUrlToSitemap(xml, "/blog", "daily", "0.9");
        addUrlToSitemap(xml, "/donate", "weekly", "0.9");
        addUrlToSitemap(xml, "/sponsor", "weekly", "0.9");

        // Add blog posts
        for (Blog blog : blogs) {
            addBlogUrlToSitemap(xml, blog);
        }

        xml.append("</urlset>");

        return ResponseEntity.ok()
                .header("Content-Type", "application/xml")
                .body(xml.toString());
    }

    @GetMapping(value = "/public/sitemap.gz", produces = "application/gzip")
    public ResponseEntity<byte[]> getCompressedSitemap() throws IOException {
        String sitemapXml = getSitemapXml().getBody();

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        GZIPOutputStream gzipOut = new GZIPOutputStream(baos);
        gzipOut.write(sitemapXml.getBytes(StandardCharsets.UTF_8));
        gzipOut.close();

        return ResponseEntity.ok()
                .header("Content-Type", "application/gzip")
                .header("Content-Encoding", "gzip")
                .body(baos.toByteArray());
    }

    @GetMapping("/public/sitemap")
    public ResponseEntity<Map<String, Object>> getSitemapJson() {
        log.info("Generating JSON sitemap");

        List<Blog> blogs = blogRepository.findByPublishedTrueOrderByUpdatedAtDesc();

        List<Map<String, String>> blogUrls = blogs.stream().map(blog -> {
            Map<String, String> item = new HashMap<>();
            item.put("loc", "/blog/" + blog.getSlug());
            item.put("lastmod", blog.getUpdatedAt() != null ?
                    blog.getUpdatedAt().toString() : blog.getCreatedAt().toString());
            item.put("changefreq", "weekly");
            item.put("priority", blog.isFeatured() ? "0.8" : "0.6");
            return item;
        }).collect(Collectors.toList());

        Map<String, Object> response = new HashMap<>();
        response.put("staticPages", getStaticPages());
        response.put("blogs", blogUrls);
        response.put("totalBlogs", blogs.size());
        response.put("lastGenerated", LocalDateTime.now().toString());

        return ResponseEntity.ok(response);
    }

    @GetMapping("/public/sitemap-index.xml")
    public ResponseEntity<String> getSitemapIndex() {
        StringBuilder xml = new StringBuilder();
        xml.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        xml.append("<sitemapindex xmlns=\"http://www.sitemaps.org/schemas/sitemap/0.9\">\n");

        xml.append("<sitemap>\n");
        xml.append("  <loc>https://liftakids.com/api/blogs/public/sitemap.xml</loc>\n");
        xml.append("  <lastmod>").append(LocalDate.now()).append("</lastmod>\n");
        xml.append("</sitemap>\n");

        xml.append("</sitemapindex>");

        return ResponseEntity.ok()
                .header("Content-Type", "application/xml")
                .body(xml.toString());
    }

    @GetMapping("/public/sitemap/page/{page}")
    public ResponseEntity<Map<String, Object>> getSitemapPaginated(
            @PathVariable int page,
            @RequestParam(defaultValue = "100") int size) {

        log.info("Generating paginated sitemap - page: {}, size: {}", page, size);

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "updatedAt"));
        Page<Blog> blogPage = blogRepository.findByPublishedTrueOrderByUpdatedAtDesc(pageable);

        List<Map<String, String>> blogUrls = blogPage.getContent().stream().map(blog -> {
            Map<String, String> item = new HashMap<>();
            item.put("loc", "/blog/" + blog.getSlug());
            item.put("lastmod", blog.getUpdatedAt() != null ?
                    blog.getUpdatedAt().toString() : blog.getCreatedAt().toString());
            item.put("changefreq", "weekly");
            item.put("priority", blog.isFeatured() ? "0.8" : "0.6");
            return item;
        }).collect(Collectors.toList());

        Map<String, Object> response = new HashMap<>();
        response.put("currentPage", page);
        response.put("totalPages", blogPage.getTotalPages());
        response.put("totalBlogs", blogPage.getTotalElements());
        response.put("blogs", blogUrls);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/public/sitemap/recent")
    public ResponseEntity<Map<String, Object>> getRecentSitemap() {
        log.info("Generating recent blogs sitemap");

        LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);
        List<Blog> recentBlogs = blogRepository.findRecentPublishedBlogs(thirtyDaysAgo);

        List<Map<String, String>> blogUrls = recentBlogs.stream().map(blog -> {
            Map<String, String> item = new HashMap<>();
            item.put("loc", "/blog/" + blog.getSlug());
            item.put("lastmod", blog.getUpdatedAt() != null ?
                    blog.getUpdatedAt().toString() : blog.getCreatedAt().toString());
            item.put("changefreq", "weekly");
            item.put("priority", blog.isFeatured() ? "0.8" : "0.6");
            return item;
        }).collect(Collectors.toList());

        Map<String, Object> response = new HashMap<>();
        response.put("totalRecentBlogs", recentBlogs.size());
        response.put("since", thirtyDaysAgo.toString());
        response.put("blogs", blogUrls);

        return ResponseEntity.ok(response);
    }

    // ============= ROBOTS.TXT =============

    @GetMapping(value = "/robots.txt", produces = "text/plain")
    public ResponseEntity<String> getRobotsTxt() {
        String robotsTxt = """
            User-agent: *
            Allow: /
            
            # Sitemap location
            Sitemap: https://liftakids.com/api/blogs/public/sitemap.xml
            
            # Disallow admin and private paths
            Disallow: /admin/
            Disallow: /api/admin/
            Disallow: /dashboard/
            Disallow: /login/
            Disallow: /register/
            
            # Crawl delay for heavy pages
            Crawl-delay: 1
            
            # Specify host
            Host: https://liftakids.com
            """;

        return ResponseEntity.ok()
                .header("Content-Type", "text/plain")
                .body(robotsTxt);
    }

    // ============= PRIVATE HELPER METHODS =============

    private Map<String, String> generateSeoMeta(BlogResponseDto blog) {
        Map<String, String> seoMeta = new HashMap<>();

        // Basic Meta Tags
        seoMeta.put("title", blog.getMetaTitle() != null ? blog.getMetaTitle() : blog.getTitle());
        seoMeta.put("description", blog.getMetaDescription() != null ? blog.getMetaDescription() : blog.getShortDescription());
        seoMeta.put("keywords", blog.getMetaKeywords() != null ? blog.getMetaKeywords() : "");
        seoMeta.put("canonical", blog.getCanonicalUrl() != null ? blog.getCanonicalUrl() : "/blog/" + blog.getSlug());

        // Open Graph / Facebook Meta Tags
        seoMeta.put("og:title", blog.getTitle());
        seoMeta.put("og:description", blog.getMetaDescription() != null ? blog.getMetaDescription() : blog.getShortDescription());
        seoMeta.put("og:image", blog.getOgImage() != null ? blog.getOgImage() : blog.getFeaturedImage());
        seoMeta.put("og:url", "https://liftakids.com/blog/" + blog.getSlug());
        seoMeta.put("og:type", blog.getOgType() != null ? blog.getOgType() : "article");
        seoMeta.put("og:site_name", "Lift A Kid");
        seoMeta.put("og:locale", "en_US");

        // Twitter Card Meta Tags
        seoMeta.put("twitter:card", blog.getTwitterCard() != null ? blog.getTwitterCard() : "summary_large_image");
        seoMeta.put("twitter:title", blog.getTitle());
        seoMeta.put("twitter:description", blog.getMetaDescription() != null ? blog.getMetaDescription() : blog.getShortDescription());
        seoMeta.put("twitter:image", blog.getOgImage() != null ? blog.getOgImage() : blog.getFeaturedImage());
        seoMeta.put("twitter:site", "@LiftAKid");
        seoMeta.put("twitter:creator", "@" + blog.getAuthor());

        // Robots Meta Tags
        seoMeta.put("robots", (blog.isIndex() ? "index" : "noindex") + ", " + (blog.isFollow() ? "follow" : "nofollow"));

        // Additional SEO Meta Tags
        seoMeta.put("author", blog.getAuthor());
        seoMeta.put("article:published_time", blog.getPublishedAt() != null ? blog.getPublishedAt().toString() : "");
        seoMeta.put("article:modified_time", blog.getUpdatedAt() != null ? blog.getUpdatedAt().toString() : "");
        seoMeta.put("article:section", blog.getCategory() != null ? blog.getCategory() : "General");

        return seoMeta;
    }

    private void addUrlToSitemap(StringBuilder xml, String loc, String changefreq, String priority) {
        xml.append("<url>\n");
        xml.append("  <loc>https://liftakids.com").append(loc).append("</loc>\n");
        xml.append("  <changefreq>").append(changefreq).append("</changefreq>\n");
        xml.append("  <priority>").append(priority).append("</priority>\n");
        xml.append("</url>\n");
    }

    private void addBlogUrlToSitemap(StringBuilder xml, Blog blog) {
        xml.append("<url>\n");
        xml.append("  <loc>https://liftakids.com/blog/").append(blog.getSlug()).append("</loc>\n");
        xml.append("  <lastmod>").append(blog.getUpdatedAt() != null ? blog.getUpdatedAt() : blog.getCreatedAt()).append("</lastmod>\n");
        xml.append("  <changefreq>weekly</changefreq>\n");
        xml.append("  <priority>").append(blog.isFeatured() ? "0.8" : "0.6").append("</priority>\n");
        xml.append("</url>\n");
    }

    private List<Map<String, String>> getStaticPages() {
        List<Map<String, String>> staticPages = new ArrayList<>();

        String[][] pages = {
                {"/", "daily", "1.0"},
                {"/about", "monthly", "0.8"},
                {"/contact", "monthly", "0.8"},
                {"/blog", "daily", "0.9"},
                {"/donate", "weekly", "0.9"},
                {"/sponsor", "weekly", "0.9"},
                {"/how-it-works", "monthly", "0.7"},
                {"/faq", "monthly", "0.7"},
                {"/success-stories", "weekly", "0.8"},
                {"/our-impact", "weekly", "0.8"},
                {"/become-volunteer", "monthly", "0.6"},
                {"/privacy-policy", "yearly", "0.3"},
                {"/terms-of-service", "yearly", "0.3"}
        };

        for (String[] page : pages) {
            Map<String, String> item = new HashMap<>();
            item.put("loc", page[0]);
            item.put("changefreq", page[1]);
            item.put("priority", page[2]);
            staticPages.add(item);
        }

        return staticPages;
    }
}