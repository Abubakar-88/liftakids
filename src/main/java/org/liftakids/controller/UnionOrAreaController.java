package org.liftakids.controller;

import lombok.RequiredArgsConstructor;
import org.liftakids.dto.unionOrArea.UnionOrAreaDto;
import org.liftakids.service.UnionOrAreaService;
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
    public ResponseEntity<UnionOrAreaDto> create(@RequestBody UnionOrAreaDto dto) {
        return new ResponseEntity<>(unionOrAreaService.create(dto), HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<UnionOrAreaDto>> getAll() {
        return ResponseEntity.ok(unionOrAreaService.getAll());
    }
}
