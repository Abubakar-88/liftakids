package org.liftakids.controller;

import jakarta.validation.Valid;
import org.liftakids.dto.pages.StaticPageRequestDTO;
import org.liftakids.dto.pages.StaticPageResponseDTO;
import org.liftakids.entity.StaticPage;
import org.liftakids.service.StaticPageService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/pages")
@Validated
public class StaticPageController {

    @Autowired
    private StaticPageService pageService;

    @Autowired
    private ModelMapper modelMapper;

    // Get published page by slug (for public)
    @GetMapping("/{slug}")
    public ResponseEntity<StaticPageResponseDTO> getPageBySlug(@PathVariable String slug) {
        return pageService.getPageBySlug(slug)
                .map(this::convertToDto)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // Get all pages (for admin)
    @GetMapping
    public ResponseEntity<List<StaticPageResponseDTO>> getAllPages() {
        List<StaticPageResponseDTO> pages = pageService.getAllPages()
                .stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(pages);
    }

    // Create or update page
    @PostMapping
    public ResponseEntity<StaticPageResponseDTO> savePage(
            @Valid @RequestBody StaticPageRequestDTO pageRequest) {

        // Check if slug is unique (for new pages)
        if (pageRequest.getId() == null && pageService.slugExists(pageRequest.getSlug())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Slug already exists");
        }

        StaticPage page = convertToEntity(pageRequest);
        StaticPage savedPage = pageService.savePage(page);
        StaticPageResponseDTO responseDto = convertToDto(savedPage);

        return ResponseEntity.ok(responseDto);
    }

    // Delete page
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deletePage(@PathVariable Long id) {
        if (!pageService.getPageById(id).isPresent()) {
            return ResponseEntity.notFound().build();
        }

        pageService.deletePage(id);
        return ResponseEntity.ok().build();
    }

    // Get page by ID (for admin)
    @GetMapping("/admin/{id}")
    public ResponseEntity<StaticPageResponseDTO> getPageById(@PathVariable Long id) {
        return pageService.getPageById(id)
                .map(this::convertToDto)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // DTO conversion methods
    private StaticPageResponseDTO convertToDto(StaticPage page) {
        return modelMapper.map(page, StaticPageResponseDTO.class);
    }

    private StaticPage convertToEntity(StaticPageRequestDTO pageRequest) {
        return modelMapper.map(pageRequest, StaticPage.class);
    }

    // Create new custom page
    @PostMapping("/create")
    public ResponseEntity<StaticPageResponseDTO> createCustomPage(
            @Valid @RequestBody StaticPageRequestDTO pageRequest) {

        // Check if slug already exists
        if (pageService.slugExists(pageRequest.getSlug())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Slug already exists");
        }

        // Validate slug format
        if (!isValidSlug(pageRequest.getSlug())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid slug format. Use only letters, numbers, and hyphens");
        }

        StaticPage page = convertToEntity(pageRequest);
        StaticPage savedPage = pageService.savePage(page);
        StaticPageResponseDTO responseDto = convertToDto(savedPage);

        return ResponseEntity.status(HttpStatus.CREATED).body(responseDto);
    }

    // Check slug availability
    @GetMapping("/check-slug/{slug}")
    public ResponseEntity<Map<String, Object>> checkSlugAvailability(@PathVariable String slug) {
        boolean exists = pageService.slugExists(slug);
        boolean valid = isValidSlug(slug);

        Map<String, Object> response = new HashMap<>();
        response.put("available", !exists);
        response.put("valid", valid);
        response.put("slug", slug);

        return ResponseEntity.ok(response);
    }

    private boolean isValidSlug(String slug) {
        // Allow only letters, numbers, and hyphens
        return slug != null && slug.matches("^[a-zA-Z0-9-]+$");
    }


    // Create contact page
    @PostMapping("/create-contact-page")
    public ResponseEntity<?> createContactPage() {
        try {
            // Check if contact page already exists
            if (pageService.slugExists("contact")) {
                return ResponseEntity.badRequest()
                        .body(Map.of("success", false, "message", "Contact page already exists"));
            }

            // Create default contact page
            StaticPage contactPage = new StaticPage();
            contactPage.setSlug("contact");
            contactPage.setTitle("Contact Us");
            contactPage.setContactPage(true);
            contactPage.setPublished(true);
            contactPage.setSortOrder(10);

            // Default contact page content with form
            String defaultContent = """
            <div class="contact-page">
                <div class="text-center mb-12">
                    <h1 class="text-4xl font-bold text-gray-900 mb-4">Get In Touch</h1>
                    <p class="text-xl text-gray-600 max-w-2xl mx-auto">
                        We'd love to hear from you. Send us a message and we'll respond as soon as possible.
                    </p>
                </div>
                
                <!-- Contact Form will be automatically rendered here -->
                <div id="contact-form-container"></div>
                
                <div class="mt-16 grid grid-cols-1 md:grid-cols-3 gap-8">
                    <div class="text-center">
                        <div class="bg-blue-100 p-4 rounded-full w-16 h-16 mx-auto mb-4 flex items-center justify-center">
                            <svg class="w-8 h-8 text-blue-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M3 5a2 2 0 012-2h3.28a1 1 0 01.948.684l1.498 4.493a1 1 0 01-.502 1.21l-2.257 1.13a11.042 11.042 0 005.516 5.516l1.13-2.257a1 1 0 011.21-.502l4.493 1.498a1 1 0 01.684.949V19a2 2 0 01-2 2h-1C9.716 21 3 14.284 3 6V5z" />
                            </svg>
                        </div>
                        <h3 class="text-lg font-semibold mb-2">Phone</h3>
                        <p class="text-gray-600">+880 XXXX-XXXXXX</p>
                    </div>
                    
                    <div class="text-center">
                        <div class="bg-green-100 p-4 rounded-full w-16 h-16 mx-auto mb-4 flex items-center justify-center">
                            <svg class="w-8 h-8 text-green-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M3 8l7.89 5.26a2 2 0 002.22 0L21 8M5 19h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v10a2 2 0 002 2z" />
                            </svg>
                        </div>
                        <h3 class="text-lg font-semibold mb-2">Email</h3>
                        <p class="text-gray-600">info@liftakids.org</p>
                    </div>
                    
                    <div class="text-center">
                        <div class="bg-purple-100 p-4 rounded-full w-16 h-16 mx-auto mb-4 flex items-center justify-center">
                            <svg class="w-8 h-8 text-purple-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M17.657 16.657L13.414 20.9a1.998 1.998 0 01-2.827 0l-4.244-4.243a8 8 0 1111.314 0z" />
                                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 11a3 3 0 11-6 0 3 3 0 016 0z" />
                            </svg>
                        </div>
                        <h3 class="text-lg font-semibold mb-2">Address</h3>
                        <p class="text-gray-600">Dhaka, Bangladesh</p>
                    </div>
                </div>
            </div>
            """;

            contactPage.setContent(defaultContent);
            contactPage.setMetaTitle("Contact Us - LiftAKids");
            contactPage.setMetaDescription("Get in touch with LiftAKids. We're here to help with any questions or concerns about our programs and initiatives.");
            contactPage.setMetaKeywords("contact, email, phone, address, LiftAKids");

            StaticPage savedPage = pageService.savePage(contactPage);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Contact page created successfully",
                    "page", convertToDto(savedPage)
            ));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false, "message", "Failed to create contact page"));
        }
    }

    // Check contact page exists
    @GetMapping("/check-contact-page")
    public ResponseEntity<?> checkContactPage() {
        boolean exists = pageService.slugExists("contact");
        Map<String, Object> response = new HashMap<>();
        response.put("exists", exists);

        if (exists) {
            pageService.getPageBySlug("contact").ifPresent(page -> {
                response.put("page", convertToDto(page));
            });
        }

        return ResponseEntity.ok(response);
    }



}
