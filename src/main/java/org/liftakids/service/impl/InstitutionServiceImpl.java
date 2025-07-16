package org.liftakids.service.impl;

import lombok.RequiredArgsConstructor;
import org.liftakids.dto.institute.InstitutionRequestDto;
import org.liftakids.dto.institute.InstitutionResponseDto;
import org.liftakids.entity.InstitutionType;
import org.liftakids.entity.Institutions;
import org.liftakids.entity.address.UnionOrArea;

import org.liftakids.repositories.InstitutionRepository;
import org.liftakids.repositories.UnionOrAreaRepository;
import org.liftakids.service.InstitutionService;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class InstitutionServiceImpl implements InstitutionService {

    private final InstitutionRepository institutionRepository;
    private final UnionOrAreaRepository unionOrAreaRepository;
    private final ModelMapper modelMapper;

    @Override
    public InstitutionResponseDto createInstitution(InstitutionRequestDto requestDto) {
        Institutions institution = modelMapper.map(requestDto, Institutions.class);

        institution.setInstitutionsId(null);

        UnionOrArea unionOrArea = unionOrAreaRepository.findById(requestDto.getUnionOrAreaId())
                .orElseThrow(() -> new RuntimeException("UnionOrArea not found with id " + requestDto.getUnionOrAreaId()));

        institution.setUnionOrArea(unionOrArea);
        institution.setRegistrationDate(LocalDateTime.now());
        institution.setUpdateDate(LocalDateTime.now());

        Institutions savedInstitution = institutionRepository.save(institution);

        return modelMapper.map(savedInstitution, InstitutionResponseDto.class);
    }


    @Override
    public List<InstitutionResponseDto> getAllInstitutions() {
        return institutionRepository.findAll().stream()
                .map(institution -> modelMapper.map(institution, InstitutionResponseDto.class))
                .collect(Collectors.toList());
    }

    @Override
    public InstitutionResponseDto getInstitutionById(Long id) {
        Institutions institution = institutionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Institution not found with id " + id));

        return modelMapper.map(institution, InstitutionResponseDto.class);
    }
    @Override
    public InstitutionResponseDto updateInstitution(Long id, InstitutionRequestDto requestDto) {
        Institutions existing = institutionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Institution not found with id " + id));

        modelMapper.map(requestDto, existing);

        UnionOrArea unionOrArea = unionOrAreaRepository.findById(requestDto.getUnionOrAreaId())
                .orElseThrow(() -> new RuntimeException("UnionOrArea not found with id " + requestDto.getUnionOrAreaId()));
        existing.setUnionOrArea(unionOrArea);

        existing.setUpdateDate(LocalDateTime.now());

        Institutions updated = institutionRepository.save(existing);
        return modelMapper.map(updated, InstitutionResponseDto.class);
    }

    @Override
    public void deleteInstitution(Long id) {
        if (!institutionRepository.existsById(id)) {
            throw new RuntimeException("Institution not found with id " + id);
        }
        institutionRepository.deleteById(id);
    }

    @Override
    public List<InstitutionResponseDto> getApprovedInstitutions() {
        return institutionRepository.findByApprovedTrue()
                .stream()
                .map(institution -> modelMapper.map(institution, InstitutionResponseDto.class))
                .collect(Collectors.toList());
    }

    @Override
    public List<InstitutionResponseDto> getInstitutionsByType(String type) {
        InstitutionType institutionType;
        try {
            institutionType = InstitutionType.valueOf(type.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Invalid Institution type: " + type);
        }

        return institutionRepository.findByType(institutionType)
                .stream()
                .map(institution -> modelMapper.map(institution, InstitutionResponseDto.class))
                .collect(Collectors.toList());
    }

    @Override
    public InstitutionResponseDto getByIdOrName(String value) {
        Institutions institution;

        try {
            // Try parsing as ID (number)
            Long id = Long.parseLong(value);
            institution = institutionRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Institution not found with id " + id));
        } catch (NumberFormatException e) {
            // If not a number, treat as name
            institution = institutionRepository.findByInstitutionName(value)
                    .orElseThrow(() -> new RuntimeException("Institution not found with name: " + value));
        }
        return modelMapper.map(institution, InstitutionResponseDto.class);
    }
}
