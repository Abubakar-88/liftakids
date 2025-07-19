package org.liftakids.controller;

import lombok.RequiredArgsConstructor;
import org.liftakids.dto.district.DistrictDto;
import org.liftakids.service.DistrictService;
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
    public ResponseEntity<DistrictDto> create(@RequestBody DistrictDto dto) {
        return new ResponseEntity<>(districtService.create(dto), HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<DistrictDto>> getAll() {
        return ResponseEntity.ok(districtService.getAll());
    }

    @PostMapping("/bulk")
    public ResponseEntity<List<DistrictDto>> createMultiple(@RequestBody List<DistrictDto> dtoList) {
        return ResponseEntity.ok(districtService.createMultiple(dtoList));
    }

}
