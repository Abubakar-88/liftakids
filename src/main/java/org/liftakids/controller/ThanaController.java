package org.liftakids.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.liftakids.dto.thana.ThanaDto;
import org.liftakids.dto.thana.ThanaResponseDTO;
import org.liftakids.service.ThanaService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/thanas")
@RequiredArgsConstructor
public class ThanaController {
    private final ThanaService thanaService;

    @PostMapping
    public ResponseEntity<ThanaDto> create(@RequestBody ThanaDto dto) {
        return new ResponseEntity<>(thanaService.create(dto), HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<ThanaResponseDTO>> getAll() {
        return ResponseEntity.ok(thanaService.getAll());
    }


    @GetMapping("/all")
    public ResponseEntity<Map<String, Object>> getAllThanas(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("thanaId").ascending());
        Page<ThanaResponseDTO> responsePage = thanaService.getAllThanas(pageable);

        Map<String, Object> response = new HashMap<>();
        response.put("content", responsePage.getContent());
        response.put("currentPage", responsePage.getNumber());
        response.put("totalPages", responsePage.getTotalPages());
        response.put("totalElements", responsePage.getTotalElements());
        response.put("size", responsePage.getSize());
        response.put("first", responsePage.isFirst());
        response.put("last", responsePage.isLast());
        response.put("numberOfElements", responsePage.getNumberOfElements());

        return ResponseEntity.ok(response);
    }

    @GetMapping("/district/{districtId}/thanas")
    public ResponseEntity<List<ThanaResponseDTO>> getThanasByDistrict(@PathVariable Long districtId) {
        return ResponseEntity.ok(thanaService.getThanasByDistrictId(districtId));
    }
    @PutMapping("/{thanaId}")
    public ThanaResponseDTO updateThana(
            @PathVariable Long thanaId,
            @Valid @RequestBody ThanaDto ThanaDto) {
        return thanaService.updateThana(thanaId, ThanaDto);
    }
}
