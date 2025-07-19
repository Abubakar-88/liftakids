package org.liftakids.service.impl;

import lombok.RequiredArgsConstructor;
import org.liftakids.dto.unionOrArea.UnionOrAreaDto;
import org.liftakids.entity.address.Thanas;
import org.liftakids.entity.address.UnionOrArea;
import org.liftakids.repositories.ThanaRepository;
import org.liftakids.repositories.UnionOrAreaRepository;
import org.liftakids.service.UnionOrAreaService;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UnionOrAreaServiceImpl implements UnionOrAreaService {

    private final UnionOrAreaRepository unionOrAreaRepository;
    private final ThanaRepository thanaRepository;
    private final ModelMapper modelMapper;

    @Override
    public UnionOrAreaDto create(UnionOrAreaDto dto) {
        UnionOrArea union = new UnionOrArea();
        union.setUninorOrAreaName(dto.getUnionOrAreaName());

        Thanas thana = thanaRepository.findById(dto.getThanaId())
                .orElseThrow(() -> new RuntimeException("Thana not found"));

        union.setThana(thana);
        UnionOrArea saved = unionOrAreaRepository.save(union);

        UnionOrAreaDto responseDto = modelMapper.map(saved, UnionOrAreaDto.class);
        responseDto.setThanaId(saved.getThana().getThanaId());

        return responseDto;
    }

    @Override
    public List<UnionOrAreaDto> getAll() {
        return unionOrAreaRepository.findAll().stream()
                .map(u -> {
                    UnionOrAreaDto dto = modelMapper.map(u, UnionOrAreaDto.class);
                    dto.setThanaId(u.getThana().getThanaId());
                    return dto;
                })
                .collect(Collectors.toList());
    }
}
