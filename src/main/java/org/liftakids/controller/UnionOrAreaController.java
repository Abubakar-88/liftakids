package org.liftakids.controller;

import lombok.RequiredArgsConstructor;
import org.liftakids.dto.unionOrArea.UnionOrAreaDto;
import org.liftakids.dto.unionOrArea.UnionOrAreaResponseDTO;
import org.liftakids.service.UnionOrAreaService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/unions-or-areas")
@RequiredArgsConstructor
public class UnionOrAreaController {
    private final UnionOrAreaService unionOrAreaService;

    @PostMapping
    public ResponseEntity<UnionOrAreaResponseDTO> create(@RequestBody UnionOrAreaDto dto) {
        return new ResponseEntity<>(unionOrAreaService.create(dto), HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<UnionOrAreaResponseDTO>> getAll() {
        return ResponseEntity.ok(unionOrAreaService.getAll());
    }
    @GetMapping("/all")
    public ResponseEntity<Page<UnionOrAreaResponseDTO>> getAllUnionsPaginated(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size,
            @RequestParam(defaultValue = "unionOrAreaId,asc") String sort) {

        String[] sortParams = sort.split(",");
        String property = sortParams[0];
        Sort.Direction direction = sortParams.length > 1 && sortParams[1].equalsIgnoreCase("desc")
                ? Sort.Direction.DESC
                : Sort.Direction.ASC;

        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, property));
        return ResponseEntity.ok(unionOrAreaService.getAllUnions(pageable));
    }

    @GetMapping("/by-thana-page")
    public ResponseEntity<Page<UnionOrAreaResponseDTO>> getUnionsByThanaId(
            @RequestParam Long thanaId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("unionOrAreaId").ascending());
        Page<UnionOrAreaResponseDTO> unions = unionOrAreaService.getUnionsByThanaId(thanaId, pageable);
        return ResponseEntity.ok(unions);
    }
    @GetMapping("/thana/{thanaId}")
    public ResponseEntity<List<UnionOrAreaResponseDTO>> getUnionsByThana(@PathVariable Long thanaId) {
        return ResponseEntity.ok(unionOrAreaService.getUnionsByThanaId(thanaId));
    }

}
