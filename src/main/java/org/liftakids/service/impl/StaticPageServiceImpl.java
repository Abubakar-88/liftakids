package org.liftakids.service.impl;

import jakarta.transaction.Transactional;
import org.liftakids.entity.StaticPage;
import org.liftakids.repositories.StaticPageRepository;
import org.liftakids.service.StaticPageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class StaticPageServiceImpl implements StaticPageService {

    @Autowired
    private StaticPageRepository pageRepository;

    @Override
    public Optional<StaticPage> getPageBySlug(String slug) {
        return pageRepository.findBySlugAndPublished(slug, true);
    }

    @Override
    public List<StaticPage> getAllPages() {
        return pageRepository.findAllByOrderBySortOrderAsc();
    }

    @Override
    public Optional<StaticPage> getPageById(Long id) {
        return pageRepository.findById(id);
    }

    @Override
    public StaticPage savePage(StaticPage page) {
        // Set timestamps
        if (page.getId() == null) {
            page.setCreatedAt(LocalDateTime.now());
        }
        page.setUpdatedAt(LocalDateTime.now());

        return pageRepository.save(page);
    }

    @Override
    public void deletePage(Long id) {
        pageRepository.deleteById(id);
    }

    @Override
    public boolean slugExists(String slug) {
        return pageRepository.existsBySlug(slug);
    }

    @Override
    public List<StaticPage> getPublishedPages() {
        return pageRepository.findByPublishedTrueOrderBySortOrderAsc();
    }
}
