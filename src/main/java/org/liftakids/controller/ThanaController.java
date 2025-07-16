package org.liftakids.controller;

import lombok.RequiredArgsConstructor;
import org.liftakids.dto.thana.ThanaDto;
import org.liftakids.service.ThanaService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
    public ResponseEntity<List<ThanaDto>> getAll() {
        return ResponseEntity.ok(thanaService.getAll());
    }
}
