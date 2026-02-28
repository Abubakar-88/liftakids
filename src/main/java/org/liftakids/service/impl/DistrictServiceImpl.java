package org.liftakids.service.impl;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.liftakids.dto.district.DistrictDto;
import org.liftakids.dto.district.DistrictResponseDTO;
import org.liftakids.dto.thana.ThanaResponseDTO;
import org.liftakids.dto.unionOrArea.UnionOrAreaResponseDTO;
import org.liftakids.entity.address.Districts;
import org.liftakids.entity.address.Divisions;
import org.liftakids.entity.address.Thanas;
import org.liftakids.entity.address.UnionOrArea;
import org.liftakids.repositories.DistrictRepository;
import org.liftakids.repositories.DivisionRepository;
import org.liftakids.repositories.ThanaRepository;
import org.liftakids.service.DistrictService;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DistrictServiceImpl implements DistrictService {

    private final DistrictRepository districtRepository;
    private final DivisionRepository divisionRepository;
    private final ThanaRepository thanaRepository;
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

    @Override
    public List<DistrictResponseDTO> getDistrictsByDivisionId(Long divisionId) {
        // 1. Batch এ district ids সংগ্রহ করুন
        List<Districts> districts = districtRepository.findByDivisionId(divisionId);
        List<Long> districtIds = districts.stream()
                .map(Districts::getDistrictId)
                .collect(Collectors.toList());

        // 2. Single query দিয়ে সব thanas নিয়ে আসুন
        List<Thanas> allThanas = thanaRepository.findByDistrictIdIn(districtIds);

        // 3. Map তৈরি করুন districtId -> List<Thanas>
        Map<Long, List<Thanas>> thanasByDistrict = allThanas.stream()
                .collect(Collectors.groupingBy(th -> th.getDistrict().getDistrictId()));

        // 4. DTO তৈরি করুন
        return districts.stream().map(district -> {
            DistrictResponseDTO dto = new DistrictResponseDTO();
            dto.setDistrictId(district.getDistrictId());
            dto.setDistrictName(district.getDistrictName());

            if (district.getDivision() != null) {
                dto.setDivisionId(district.getDivision().getDivisionId());
                dto.setDivisionName(district.getDivision().getDivisionName());
            }

            // 5. Map থেকে thanas নিন
            List<Thanas> districtThanas = thanasByDistrict.getOrDefault(
                    district.getDistrictId(),
                    Collections.emptyList()
            );

            Set<ThanaResponseDTO> thanaDTOs = districtThanas.stream()
                    .map(th -> {
                        ThanaResponseDTO thDTO = new ThanaResponseDTO();
                        thDTO.setThanaId(th.getThanaId());
                        thDTO.setThanaName(th.getThanaName());
                        thDTO.setDistrictId(district.getDistrictId());
                        thDTO.setDistrictName(district.getDistrictName());
                        return thDTO;
                    })
                    .collect(Collectors.toSet());

            dto.setThanas(thanaDTOs);
            return dto;
        }).collect(Collectors.toList());
    }
    @Transactional
    @Override
    public Page<DistrictResponseDTO> getAllDistricts(Pageable pageable) {
        Page<Districts> districts = districtRepository.findAllWithFetch(pageable);

        return districts.map(district -> {
            DistrictResponseDTO dto = new DistrictResponseDTO();

            // Basic District fields
            dto.setDistrictId(district.getDistrictId());
            dto.setDistrictName(district.getDistrictName());

            // Division fields
            if (district.getDivision() != null) {
                dto.setDivisionId(district.getDivision().getDivisionId());
                dto.setDivisionName(district.getDivision().getDivisionName());
            }

            // Thanas mapping
            if (district.getThanas() != null && !district.getThanas().isEmpty()) {
                Set<ThanaResponseDTO> thanaDTOs = district.getThanas().stream()
                        .map(this::convertToThanaDTO)
                        .collect(Collectors.toSet());
                dto.setThanas(thanaDTOs);
            }

            return dto;
        });
    }

    private ThanaResponseDTO convertToThanaDTO(Thanas thana) {
        ThanaResponseDTO dto = new ThanaResponseDTO();

        // Thana basic fields
        dto.setThanaId(thana.getThanaId());
        dto.setThanaName(thana.getThanaName());

        // District fields
        if (thana.getDistrict() != null) {
            dto.setDistrictId(thana.getDistrict().getDistrictId());
            dto.setDistrictName(thana.getDistrict().getDistrictName());

            // Division fields through district
            if (thana.getDistrict().getDivision() != null) {
                dto.setDivisionId(thana.getDistrict().getDivision().getDivisionId());
                dto.setDivisionName(thana.getDistrict().getDivision().getDivisionName());
            }
        }

        // UnionOrAreas mapping
        if (thana.getUnionOrAreas() != null && !thana.getUnionOrAreas().isEmpty()) {
            List<UnionOrAreaResponseDTO> unionOrAreaDTOs = thana.getUnionOrAreas().stream()
                    .map(this::convertToUnionOrAreaDTO)
                    .collect(Collectors.toList());
            dto.setUnionOrAreas(unionOrAreaDTOs);
        }

        return dto;
    }

    private UnionOrAreaResponseDTO convertToUnionOrAreaDTO(UnionOrArea unionOrArea) {
        UnionOrAreaResponseDTO dto = new UnionOrAreaResponseDTO();

        // Union/Area basic fields
        dto.setUnionOrAreaId(unionOrArea.getUnionOrAreaId());
        dto.setUnionOrAreaName(unionOrArea.getUnionOrAreaName());

        // Thana fields
        if (unionOrArea.getThana() != null) {
            Thanas thana = unionOrArea.getThana();
            dto.setThanaId(thana.getThanaId());
            dto.setThanaName(thana.getThanaName());

            // District fields through thana
            if (thana.getDistrict() != null) {
                Districts district = thana.getDistrict();
                dto.setDistrictId(district.getDistrictId());
                dto.setDistrictName(district.getDistrictName());

                // Division fields through district
                if (district.getDivision() != null) {
                    dto.setDivisionId(district.getDivision().getDivisionId());
                    dto.setDivisionName(district.getDivision().getDivisionName());
                }
            }
        }

        return dto;
    }
//    @Override
//    public Page<DistrictResponseDTO> getAllDistricts(Pageable pageable) {
//        Page<Districts> districts = districtRepository.findAll(pageable);
//        return districts.map(district -> modelMapper.map(district, DistrictResponseDTO.class));
//    }

}
