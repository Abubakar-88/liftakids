package org.liftakids.controller;

import lombok.RequiredArgsConstructor;
import org.liftakids.dto.donor.DonorRequestDto;
import org.liftakids.dto.donor.DonorResponseDto;
import org.liftakids.service.DonorService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/donors")
@RequiredArgsConstructor
public class DonorController {

    private final DonorService donorService;

    @PostMapping
    public ResponseEntity<DonorResponseDto> createDonor(@Valid @RequestBody DonorRequestDto requestDto) {
        return new ResponseEntity<>(donorService.createDonor(requestDto), HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<DonorResponseDto>> getAllDonors() {
        return ResponseEntity.ok(donorService.getAllDonors());
    }

    @GetMapping("/{id}")
    public ResponseEntity<DonorResponseDto> getDonorById(@PathVariable Long id) {
        return ResponseEntity.ok(donorService.getDonorById(id));
    }
}
