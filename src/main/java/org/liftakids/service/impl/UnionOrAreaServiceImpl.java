package org.liftakids.service.impl;

import lombok.RequiredArgsConstructor;
import org.liftakids.dto.unionOrArea.UnionOrAreaDto;
import org.liftakids.dto.unionOrArea.UnionOrAreaResponseDTO;
import org.liftakids.entity.address.Thanas;
import org.liftakids.entity.address.UnionOrArea;
import org.liftakids.repositories.ThanaRepository;
import org.liftakids.repositories.UnionOrAreaRepository;
import org.liftakids.service.UnionOrAreaService;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
    public UnionOrAreaResponseDTO create(UnionOrAreaDto dto) {
        UnionOrArea union = new UnionOrArea();
        union.setUnionOrAreaName(dto.getUnionOrAreaName());

        Thanas thana = thanaRepository.findById(dto.getThanaId())
                .orElseThrow(() -> new RuntimeException("Thana not found"));

        union.setThana(thana);
        UnionOrArea saved = unionOrAreaRepository.save(union);

        UnionOrAreaResponseDTO responseDto = modelMapper.map(saved, UnionOrAreaResponseDTO.class);
        responseDto.setThanaId(saved.getThana().getThanaId());

        return responseDto;
    }

    @Override
    public List<UnionOrAreaResponseDTO> getAll() {
        return unionOrAreaRepository.findAll().stream()
                .map(u -> {
                    UnionOrAreaResponseDTO dto = modelMapper.map(u, UnionOrAreaResponseDTO.class);
                    dto.setThanaId(u.getThana().getThanaId());
                    return dto;
                })
                .collect(Collectors.toList());
    }

    @Override
    public Page<UnionOrAreaResponseDTO> getAllUnions(Pageable pageable) {
        return unionOrAreaRepository.findAll(pageable)
                .map(this::convertToDto);
    }
    private UnionOrAreaResponseDTO convertToDto(UnionOrArea union) {
        UnionOrAreaResponseDTO dto = new UnionOrAreaResponseDTO();

        dto.setUnionOrAreaId(union.getUnionOrAreaId());
        dto.setUnionOrAreaName(union.getUnionOrAreaName());

        if (union.getThana() != null) {
            dto.setThanaId(union.getThana().getThanaId());
            dto.setThanaName(union.getThana().getThanaName());

            if (union.getThana().getDistrict() != null) {
                dto.setDistrictId(union.getThana().getDistrict().getDistrictId());
                dto.setDistrictName(union.getThana().getDistrict().getDistrictName());

                if (union.getThana().getDistrict().getDivision() != null) {
                    dto.setDivisionId(union.getThana().getDistrict().getDivision().getDivisionId());
                    dto.setDivisionName(union.getThana().getDistrict().getDivision().getDivisionName());
                }
            }
        }

        return dto;
    }

    @Override
    public Page<UnionOrAreaResponseDTO> getUnionsByThanaId(Long thanaId, Pageable pageable) {
        Page<UnionOrArea> unionsPage = unionOrAreaRepository.findByThana_ThanaId(thanaId, pageable);

        return unionsPage.map(union -> {
            UnionOrAreaResponseDTO dto = new UnionOrAreaResponseDTO();
            dto.setUnionOrAreaId(union.getUnionOrAreaId());
            dto.setUnionOrAreaName(union.getUnionOrAreaName());

            // Handle relationships with null checks
            if (union.getThana() != null) {
                dto.setThanaId(union.getThana().getThanaId());
                dto.setThanaName(union.getThana().getThanaName());

                if (union.getThana().getDistrict() != null) {
                    dto.setDistrictId(union.getThana().getDistrict().getDistrictId());
                    dto.setDistrictName(union.getThana().getDistrict().getDistrictName());

                    if (union.getThana().getDistrict().getDivision() != null) {
                        dto.setDivisionId(union.getThana().getDistrict().getDivision().getDivisionId());
                        dto.setDivisionName(union.getThana().getDistrict().getDivision().getDivisionName());
                    }
                }
            }
            return dto;
        });
    }


    @Override
    public List<UnionOrAreaResponseDTO> getUnionsByThanaId(Long thanaId) {
        return unionOrAreaRepository.findByThana_ThanaId(thanaId).stream()
                .map(union -> {
                    UnionOrAreaResponseDTO dto = new UnionOrAreaResponseDTO();
                    dto.setUnionOrAreaId(union.getUnionOrAreaId());
                    dto.setUnionOrAreaName(union.getUnionOrAreaName());

                    // Handle Thana relationship
                    if (union.getThana() != null) {
                        dto.setThanaId(union.getThana().getThanaId());
                        dto.setThanaName(union.getThana().getThanaName());

                        // Handle District relationship
                        if (union.getThana().getDistrict() != null) {
                            dto.setDistrictId(union.getThana().getDistrict().getDistrictId());
                            dto.setDistrictName(union.getThana().getDistrict().getDistrictName());

                            // Handle Division relationship
                            if (union.getThana().getDistrict().getDivision() != null) {
                                dto.setDivisionId(union.getThana().getDistrict().getDivision().getDivisionId());
                                dto.setDivisionName(union.getThana().getDistrict().getDivision().getDivisionName());
                            }
                        }
                    }
                    return dto;
                })
                .collect(Collectors.toList());
    }
}
