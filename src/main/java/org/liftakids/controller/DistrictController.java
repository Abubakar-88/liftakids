package org.liftakids.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.liftakids.dto.district.DistrictDto;
import org.liftakids.dto.district.DistrictResponseDTO;
import org.liftakids.service.DistrictService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/districts")
@RequiredArgsConstructor
public class DistrictController {
    private final DistrictService districtService;

    @PostMapping
    public ResponseEntity<DistrictDto> create(@Valid @RequestBody DistrictDto dto) {
        DistrictDto createdDistrict = districtService.create(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdDistrict);
    }

    @GetMapping
    public ResponseEntity<List<DistrictDto>> getAll() {
        return ResponseEntity.ok(districtService.getAll());
    }

    @PostMapping("/bulk")
    public ResponseEntity<List<DistrictDto>> createMultiple(@RequestBody List<DistrictDto> dtoList) {
        return ResponseEntity.ok(districtService.createMultiple(dtoList));
    }

    @GetMapping("/divisions/{divisionId}/districts")
    public ResponseEntity<List<DistrictResponseDTO>> getDistrictsByDivision(@PathVariable Long divisionId) {
        List<DistrictResponseDTO> districts = districtService.getDistrictsByDivisionId(divisionId);
        return ResponseEntity.ok(districts);
    }
    @GetMapping("/all")
    public ResponseEntity<Page<DistrictResponseDTO>> getAllDistricts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("districtId").ascending());
        Page<DistrictResponseDTO> districts = districtService.getAllDistricts(pageable);
        return new ResponseEntity<>(districts, HttpStatus.OK);
    }
}
