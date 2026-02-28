package org.liftakids.service.impl;

import lombok.RequiredArgsConstructor;
import org.liftakids.dto.district.DistrictResponseDTO;
import org.liftakids.dto.divison.DivisionDto;
import org.liftakids.dto.divison.DivisionResponseDTO;
import org.liftakids.dto.thana.ThanaResponseDTO;
import org.liftakids.dto.unionOrArea.UnionOrAreaResponseDTO;
import org.liftakids.entity.address.Districts;
import org.liftakids.entity.address.Divisions;
import org.liftakids.entity.address.Thanas;
import org.liftakids.entity.address.UnionOrArea;
import org.liftakids.exception.ResourceNotFoundException;
import org.liftakids.repositories.DistrictRepository;
import org.liftakids.repositories.DivisionRepository;
import org.liftakids.repositories.ThanaRepository;
import org.liftakids.repositories.UnionOrAreaRepository;
import org.liftakids.service.DivisionService;
import org.modelmapper.ModelMapper;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@CacheConfig(cacheNames = "divisions")
@RequiredArgsConstructor
public class DivisionServiceImpl implements DivisionService {

    private final DivisionRepository divisionRepository;
    private final ModelMapper modelMapper;
    private final DistrictRepository districtRepository;
    private final ThanaRepository thanaRepository;
    private final UnionOrAreaRepository unionRepository;

    @Override
    public DivisionDto create(DivisionDto dto) {
        Divisions division = modelMapper.map(dto, Divisions.class);
        return modelMapper.map(divisionRepository.save(division), DivisionDto.class);
    }

    @Override
    public List<DivisionResponseDTO> getAll() {
        List<Divisions> divisions = divisionRepository.findAll();

        /* ---------- DISTRICTS ---------- */
        Map<Long, List<Districts>> districtMap = new HashMap<>();
        List<Districts> allDistricts = new ArrayList<>();

        for (Divisions div : divisions) {
            List<Districts> districts =
                    districtRepository.findByDivisionId(div.getDivisionId());

            districtMap.put(div.getDivisionId(), districts);
            allDistricts.addAll(districts);
        }

        /* ---------- THANAS ---------- */
        Map<Long, List<Thanas>> thanaMap = new HashMap<>();
        List<Thanas> allThanas = new ArrayList<>();

        for (Districts dist : allDistricts) {
            List<Thanas> thanas =
                    thanaRepository.findByDistrictId(dist.getDistrictId());

            thanaMap.put(dist.getDistrictId(), thanas);
            allThanas.addAll(thanas);
        }

        /* ---------- UNION / AREAS ---------- */
        Map<Long, List<UnionOrArea>> unionMap = new HashMap<>();

        for (Thanas th : allThanas) {
            List<UnionOrArea> unions =
                    unionRepository.findByThanaId(th.getThanaId());

            unionMap.put(th.getThanaId(), unions);
        }
        // 🧠 Build hierarchy DTO
        return divisions.stream().map(div -> {
            DivisionResponseDTO  divDTO = new DivisionResponseDTO();
            divDTO.setDivisionId(div.getDivisionId());
            divDTO.setDivisionName(div.getDivisionName());

            List<Districts> divDistricts = districtMap.getOrDefault(div.getDivisionId(), List.of());

            divDistricts.forEach(dist -> {
                DistrictResponseDTO distDTO = new DistrictResponseDTO();
                distDTO.setDistrictId(dist.getDistrictId());
                distDTO.setDistrictName(dist.getDistrictName());
                distDTO.setDivisionId(divDTO.getDivisionId());
                distDTO.setDivisionName(divDTO.getDivisionName());

                List<Thanas> distThanas = thanaMap.getOrDefault(dist.getDistrictId(), List.of());
                distThanas.forEach(th -> {
                    ThanaResponseDTO thDTO = new ThanaResponseDTO();
                    thDTO.setThanaId(th.getThanaId());
                    thDTO.setThanaName(th.getThanaName());
                    thDTO.setDistrictId(distDTO.getDistrictId());
                    thDTO.setDistrictName(distDTO.getDistrictName());
                    thDTO.setDivisionId(divDTO.getDivisionId());
                    thDTO.setDivisionName(divDTO.getDivisionName());


                    List<UnionOrArea> thUnions = unionMap.getOrDefault(th.getThanaId(), List.of());
                    thUnions.forEach(u -> {
                        UnionOrAreaResponseDTO uDTO = new UnionOrAreaResponseDTO();
                        uDTO.setUnionOrAreaId(u.getUnionOrAreaId());
                        uDTO.setUnionOrAreaName(u.getUnionOrAreaName());
                        uDTO.setThanaId(thDTO.getThanaId());
                        uDTO.setThanaName(thDTO.getThanaName());
                        uDTO.setDistrictId(distDTO.getDistrictId());
                        uDTO.setDistrictName(distDTO.getDistrictName());
                        uDTO.setDivisionId(divDTO.getDivisionId());
                        uDTO.setDivisionName(divDTO.getDivisionName());
                        thDTO.getUnionOrAreas().add(uDTO);
                    });

                    distDTO.getThanas().add(thDTO);
                });

                divDTO.getDistricts().add(distDTO);
            });

            return divDTO;
        }).toList();
//        return divisionRepository.findAll()
//                .stream()
//                .map(d -> modelMapper.map(d, DivisionResponseDTO.class))
//                .collect(Collectors.toList());
    }


//    @Override
//    @Cacheable
//    public List<DivisionResponseDTO> getAll() {
//        List<Divisions> divisions = divisionRepository.findAllBasic();
//
//        return divisions.stream()
//                .map(this::convertToDTO)
//                .collect(Collectors.toList());
//    }
//    private DivisionResponseDTO convertToDTO(Divisions division) {
//        DivisionResponseDTO dto = new DivisionResponseDTO();
//        dto.setDivisionId(division.getDivisionId());
//        dto.setDivisionName(division.getDivisionName());
//
//        List<DistrictResponseDTO> districtDTOs = division.getDistricts().stream()
//                .map(district -> {
//                    DistrictResponseDTO districtDTO = new DistrictResponseDTO();
//                    districtDTO.setDistrictId(district.getDistrictId());
//                    districtDTO.setDistrictName(district.getDistrictName());
//                    districtDTO.setDivisionId(division.getDivisionId());
//                    districtDTO.setDivisionName(division.getDivisionName());
//
//                    // Only process thanas if they exist and we need them
//                    if (district.getThanas() != null && !district.getThanas().isEmpty()) {
//                        List<ThanaResponseDTO> thanaDTOs = district.getThanas().stream()
//                                .map(thana -> {
//                                    ThanaResponseDTO thanaDTO = new ThanaResponseDTO();
//                                    thanaDTO.setThanaId(thana.getThanaId());
//                                    thanaDTO.setThanaName(thana.getThanaName());
//                                    thanaDTO.setDistrictId(district.getDistrictId());
//                                    thanaDTO.setDistrictName(district.getDistrictName());
//                                    thanaDTO.setDivisionId(division.getDivisionId());
//                                    thanaDTO.setDivisionName(division.getDivisionName());
//
//                                    if (thana.getUnionOrAreas() != null && !thana.getUnionOrAreas().isEmpty()) {
//                                        List<UnionOrAreaResponseDTO> unionDTOs = thana.getUnionOrAreas().stream()
//                                                .map(union -> {
//                                                    UnionOrAreaResponseDTO unionDTO = new UnionOrAreaResponseDTO();
//                                                    unionDTO.setUnionOrAreaId(union.getUnionOrAreaId());
//                                                    unionDTO.setUnionOrAreaName(union.getUnionOrAreaName());
//                                                    unionDTO.setThanaName(thana.getThanaName());
//                                                    unionDTO.setThanaId(thana.getThanaId());
//                                                    unionDTO.setDistrictId(district.getDistrictId());
//                                                    unionDTO.setDistrictName(district.getDistrictName());
//                                                    unionDTO.setDivisionId(division.getDivisionId());
//                                                    unionDTO.setDivisionName(division.getDivisionName());
//                                                    return unionDTO;
//                                                })
//                                                .collect(Collectors.toList());
//                                        thanaDTO.setUnionOrAreas(unionDTOs);
//                                    } else {
//                                        thanaDTO.setUnionOrAreas(Collections.emptyList());
//                                    }
//
//                                    return thanaDTO;
//                                })
//                                .collect(Collectors.toList());
//                        districtDTO.setThanas(thanaDTOs);
//                    } else {
//                        districtDTO.setThanas(Collections.emptyList());
//                    }
//
//                    return districtDTO;
//                })
//                .collect(Collectors.toList());
//
//        dto.setDistricts(districtDTOs);
//        return dto;
//    }
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


    @Override
    public DivisionResponseDTO updateDivision(Long divisionId, DivisionDto divisionRequestDTO) {
        // Fetch the existing Division entity from the repository instead of using the DTO
        Divisions existingDivision = divisionRepository.findById(divisionId)
                .orElseThrow(() -> new ResourceNotFoundException("Division not found with id: " + divisionId));

        // Map the updated data from the request DTO to the existing Division entity
        modelMapper.map(divisionRequestDTO, existingDivision);

        // Save the updated entity back to the repository
        Divisions updatedDivision = divisionRepository.save(existingDivision);

        // Return the updated entity as a DTO using ModelMapper
        return modelMapper.map(updatedDivision, DivisionResponseDTO.class);
    }
}
