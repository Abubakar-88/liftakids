package org.liftakids.service.impl;

import lombok.RequiredArgsConstructor;
import org.liftakids.dto.divison.DivisionDto;
import org.liftakids.entity.address.Divisions;
import org.liftakids.repositories.DivisionRepository;
import org.liftakids.service.DivisionService;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DivisionServiceImpl implements DivisionService {

    private final DivisionRepository divisionRepository;
    private final ModelMapper modelMapper;

    @Override
    public DivisionDto create(DivisionDto dto) {
        Divisions division = modelMapper.map(dto, Divisions.class);
        return modelMapper.map(divisionRepository.save(division), DivisionDto.class);
    }

    @Override
    public List<DivisionDto> getAll() {
        return divisionRepository.findAll()
                .stream()
                .map(d -> modelMapper.map(d, DivisionDto.class))
                .collect(Collectors.toList());
    }

    @Override
    public List<DivisionDto> createAll(List<DivisionDto> divisionDtos) {
        List<Divisions> divisions = divisionDtos.stream()
                .map(dto -> modelMapper.map(dto, Divisions.class))
                .collect(Collectors.toList());

        List<Divisions> savedDivisions = divisionRepository.saveAll(divisions);

        return savedDivisions.stream()
                .map(saved -> modelMapper.map(saved, DivisionDto.class))
                .collect(Collectors.toList());
    }
}
