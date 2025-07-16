package org.liftakids.service.impl;

import lombok.RequiredArgsConstructor;
import org.liftakids.dto.thana.ThanaDto;
import org.liftakids.entity.address.Districts;
import org.liftakids.entity.address.Thanas;
import org.liftakids.repositories.DistrictRepository;
import org.liftakids.repositories.ThanaRepository;
import org.liftakids.service.ThanaService;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ThanaServiceImpl implements ThanaService {

    private final ThanaRepository thanaRepository;
    private final DistrictRepository districtRepository;
    private final ModelMapper modelMapper;

    @Override
    public ThanaDto create(ThanaDto dto) {
        Thanas thana = new Thanas();
        thana.setThanaName(dto.getThanaName());

        Districts district = districtRepository.findById(dto.getDistrictId())
                .orElseThrow(() -> new RuntimeException("District not found"));

        thana.setDistrict(district);
        Thanas saved = thanaRepository.save(thana);

        ThanaDto responseDto = modelMapper.map(saved, ThanaDto.class);
        responseDto.setDistrictId(saved.getDistrict().getDistrictId());

        return responseDto;
    }

    @Override
    public List<ThanaDto> getAll() {
        return thanaRepository.findAll().stream()
                .map(t -> {
                    ThanaDto dto = modelMapper.map(t, ThanaDto.class);
                    dto.setDistrictId(t.getDistrict().getDistrictId());
                    return dto;
                })
                .collect(Collectors.toList());
    }
}
