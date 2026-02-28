package org.liftakids.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.liftakids.dto.thana.ThanaDto;
import org.liftakids.dto.thana.ThanaResponseDTO;
import org.liftakids.dto.unionOrArea.UnionOrAreaResponseDTO;
import org.liftakids.entity.address.Districts;
import org.liftakids.entity.address.Divisions;
import org.liftakids.entity.address.Thanas;
import org.liftakids.exception.ResourceAlreadyExistsException;
import org.liftakids.exception.ResourceNotFoundException;
import org.liftakids.repositories.DistrictRepository;
import org.liftakids.repositories.ThanaRepository;
import org.liftakids.service.ThanaService;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
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
    public List<ThanaResponseDTO> getAll() {
        return thanaRepository.findAll().stream()
                .map(t -> {
                    ThanaResponseDTO dto = modelMapper.map(t, ThanaResponseDTO.class);
                    dto.setDistrictId(t.getDistrict().getDistrictId());
                    return dto;
                })
                .collect(Collectors.toList());
    }

    @Override
    public Page<ThanaResponseDTO> getAllThanas(Pageable pageable) {
        Page<Thanas> thanas = thanaRepository.findAll(pageable);
        return thanas.map(this::mapToDTO);
    }

    private ThanaResponseDTO mapToDTO(Thanas thana) {
        ThanaResponseDTO dto = new ThanaResponseDTO();
        dto.setThanaId(thana.getThanaId());
        dto.setThanaName(thana.getThanaName());

        if (thana.getDistrict() != null) {
            dto.setDistrictId(thana.getDistrict().getDistrictId());
            dto.setDistrictName(thana.getDistrict().getDistrictName());

            Divisions division = thana.getDistrict().getDivision();
            if (division != null) {
                dto.setDivisionId(division.getDivisionId());
                dto.setDivisionName(division.getDivisionName());
            }
        }

        return dto;
    }
    @Override
    public List<ThanaResponseDTO> getThanasByDistrictId(Long districtId) {
        return thanaRepository.findByDistrictId(districtId).stream()
                .map(thana -> {
                    ThanaResponseDTO dto = new ThanaResponseDTO();

                    // Map Thana fields
                    dto.setThanaId(thana.getThanaId()); // assuming your entity has getId()
                    dto.setThanaName(thana.getThanaName()); // assuming your entity has getName()

                    // Map District fields if available
                    if (thana.getDistrict() != null) {
                        dto.setDistrictId(thana.getDistrict().getDistrictId());
                        dto.setDistrictName(thana.getDistrict().getDistrictName());

                        // Map Division fields through District
                        if (thana.getDistrict().getDivision() != null) {
                            dto.setDivisionId(thana.getDistrict().getDivision().getDivisionId());
                            dto.setDivisionName(thana.getDistrict().getDivision().getDivisionName());
                        }
                    }

                    // Map UnionOrAreas if needed
                    if (thana.getUnionOrAreas() != null) {
                        List<UnionOrAreaResponseDTO> unionOrAreaDTOs = thana.getUnionOrAreas().stream()
                                .map(unionOrArea -> {
                                    UnionOrAreaResponseDTO areaDTO = new UnionOrAreaResponseDTO();
                                    areaDTO.setUnionOrAreaId(unionOrArea.getUnionOrAreaId());
                                    areaDTO.setUnionOrAreaName(unionOrArea.getUnionOrAreaName());
                                    areaDTO.setThanaName(thana.getThanaName());
                                    areaDTO.setThanaId(thana.getThanaId());

                                    // Set district and division info
                                    if (thana.getDistrict() != null) {
                                        areaDTO.setDistrictId(thana.getDistrict().getDistrictId());
                                        areaDTO.setDistrictName(thana.getDistrict().getDistrictName());

                                        if (thana.getDistrict().getDivision() != null) {
                                            areaDTO.setDivisionId(thana.getDistrict().getDivision().getDivisionId());
                                            areaDTO.setDivisionName(thana.getDistrict().getDivision().getDivisionName());
                                        }
                                    }
                                    return areaDTO;
                                })
                                .collect(Collectors.toList());

                        dto.setUnionOrAreas(unionOrAreaDTOs);
                    }

                    return dto;
                })
                .collect(Collectors.toList());
    }

    @Override
    public ThanaResponseDTO updateThana(Long thanaId, ThanaDto thanaRequestDTO) {
        // 1. Fetch the existing thana with complete relationships
        Thanas existingThana = thanaRepository.findWithDivisionById(thanaId)
                .orElseThrow(() -> new ResourceNotFoundException("Thana not found with id: " + thanaId));

        // 2. Validate district exists and get it with division
        Districts district = districtRepository.findWithDivisionByDistrictId(thanaRequestDTO.getDistrictId())
                .orElseThrow(() -> new ResourceNotFoundException("District not found with id: " + thanaRequestDTO.getDistrictId()));

        // 3. Check for duplicate thana name in the same district
        if (!existingThana.getThanaName().equalsIgnoreCase(thanaRequestDTO.getThanaName()) &&
                thanaRepository.existsByThanaNameAndDistrictDistrictId(thanaRequestDTO.getThanaName(), thanaRequestDTO.getDistrictId())) {
            throw new ResourceAlreadyExistsException("Thana name already exists in this district");
        }

        // 4. Update the thana entity
        existingThana.setThanaName(thanaRequestDTO.getThanaName());
        existingThana.setDistrict(district);

        // 5. Save and return the updated thana
        Thanas updatedThana = thanaRepository.save(existingThana);
        return convertToResponseDTO(updatedThana);
    }
    private ThanaResponseDTO convertToResponseDTO(Thanas thana) {
        ThanaResponseDTO dto = new ThanaResponseDTO();
        dto.setThanaId(thana.getThanaId());
        dto.setThanaName(thana.getThanaName());

        if (thana.getDistrict() != null) {
            dto.setDistrictId(thana.getDistrict().getDistrictId());
            dto.setDistrictName(thana.getDistrict().getDistrictName());

            Divisions division = thana.getDistrict().getDivision();
            if (division != null) {
                dto.setDivisionId(division.getDivisionId());
                dto.setDivisionName(division.getDivisionName());
            } else {
                log.warn("Division is null for district: {}", thana.getDistrict().getDistrictId());
            }
        } else {
            log.warn("District is null for thana: {}", thana.getThanaId());
        }

        return dto;
    }





}
