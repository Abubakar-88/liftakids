package org.liftakids.service;

import org.liftakids.dto.unionOrArea.UnionOrAreaDto;

import java.util.List;

public interface UnionOrAreaService {
    UnionOrAreaDto create(UnionOrAreaDto dto);
    List<UnionOrAreaDto> getAll();
}
