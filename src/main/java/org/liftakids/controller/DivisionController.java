package org.liftakids.controller;

import lombok.RequiredArgsConstructor;
import org.liftakids.dto.divison.DivisionDto;
import org.liftakids.service.DivisionService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/divisions")
@RequiredArgsConstructor
public class DivisionController {
    private final DivisionService divisionService;

    @PostMapping
    public ResponseEntity<DivisionDto> create(@RequestBody DivisionDto dto) {
        return new ResponseEntity<>(divisionService.create(dto), HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<DivisionDto>> getAll() {
        return ResponseEntity.ok(divisionService.getAll());
    }

    @PostMapping("/divisions/bulk")
    public ResponseEntity<List<DivisionDto>> createDivisions(@RequestBody List<DivisionDto> divisionDtos) {
        return ResponseEntity.ok(divisionService.createAll(divisionDtos));
    }

}
