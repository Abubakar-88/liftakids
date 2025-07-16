package org.liftakids.service;

import org.liftakids.dto.thana.ThanaDto;

import java.util.List;

public interface ThanaService {
    ThanaDto create(ThanaDto dto);
    List<ThanaDto> getAll();
}
