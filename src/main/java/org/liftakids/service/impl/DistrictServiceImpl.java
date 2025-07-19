package org.liftakids.service.impl;

import lombok.RequiredArgsConstructor;
import org.liftakids.dto.district.DistrictDto;
import org.liftakids.entity.address.Districts;
import org.liftakids.entity.address.Divisions;
import org.liftakids.repositories.DistrictRepository;
import org.liftakids.repositories.DivisionRepository;
import org.liftakids.service.DistrictService;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DistrictServiceImpl implements DistrictService {

    private final DistrictRepository districtRepository;
    private final DivisionRepository divisionRepository;
    private final ModelMapper modelMapper;

    @Override
    public DistrictDto create(DistrictDto dto) {
        Districts district = new Districts();
        district.setDistrictName(dto.getDistrictName());

        Divisions division = divisionRepository.findById(dto.getDivisionId())
                .orElseThrow(() -> new RuntimeException("Division not found"));

        district.setDivision(division);
        Districts saved = districtRepository.save(district);

        DistrictDto responseDto = modelMapper.map(saved, DistrictDto.class);
        responseDto.setDivisionId(saved.getDivision().getDivisionId());

        return responseDto;
    }

    @Override
    public List<DistrictDto> getAll() {
        return districtRepository.findAll().stream()
                .map(d -> {
                    DistrictDto dto = modelMapper.map(d, DistrictDto.class);
                    dto.setDivisionId(d.getDivision().getDivisionId());
                    return dto;
                })
                .collect(Collectors.toList());
    }
    @Override
    public List<DistrictDto> createMultiple(List<DistrictDto> dtoList) {
        List<Districts> districts = dtoList.stream().map(dto -> {
            Districts district = new Districts();
            district.setDistrictName(dto.getDistrictName());

            Divisions division = divisionRepository.findById(dto.getDivisionId())
                    .orElseThrow(() -> new RuntimeException("Division not found for ID: " + dto.getDivisionId()));

            district.setDivision(division);
            return district;
        }).toList();

        List<Districts> saved = districtRepository.saveAll(districts);

        return saved.stream().map(d -> {
            DistrictDto dto = modelMapper.map(d, DistrictDto.class);
            dto.setDivisionId(d.getDivision().getDivisionId());
            return dto;
        }).toList();
    }


}
