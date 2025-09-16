package org.liftakids.controller;

import lombok.RequiredArgsConstructor;
import org.liftakids.dto.donor.DonorRequestDto;
import org.liftakids.dto.donor.DonorResponseDto;
import org.liftakids.dto.donor.LoginRequestDto;
import org.liftakids.dto.donor.LoginResponseDto;
import org.liftakids.dto.sponsorship.SponsorshipResponseDto;
import org.liftakids.service.DonorService;
import org.liftakids.service.SponsorshipService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/donors")
@RequiredArgsConstructor
public class DonorController {
    private final SponsorshipService sponsorshipService;
    private final DonorService donorService;

    @PostMapping
    public ResponseEntity<DonorResponseDto> createDonor(@Valid @RequestBody DonorRequestDto requestDto) {
        return new ResponseEntity<>(donorService.createDonor(requestDto), HttpStatus.CREATED);
    }
    @PostMapping("/login")
    public ResponseEntity<LoginResponseDto> loginDonor(@Valid @RequestBody LoginRequestDto loginRequest) {
        LoginResponseDto response = donorService.loginDonor(loginRequest);
        return ResponseEntity.ok(response);
    }
    @GetMapping
    public ResponseEntity<List<DonorResponseDto>> getAllDonors() {
        return ResponseEntity.ok(donorService.getAllDonors());
    }

    @GetMapping("/{id}")
    public ResponseEntity<DonorResponseDto> getDonorById(@PathVariable Long id) {
        return ResponseEntity.ok(donorService.getDonorById(id));
    }

    @GetMapping("/search")
    public ResponseEntity<List<DonorResponseDto>> searchDonorsSimple(@RequestParam String searchTerm) {
        return ResponseEntity.ok(donorService.searchDonors(searchTerm));
    }
    @GetMapping("/all")
    public ResponseEntity<Page<DonorResponseDto>> getAllDonorsWithPagination(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "name") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {

        return ResponseEntity.ok(donorService.getAllDonorsWithPagination(page, size, sortBy, sortDir));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDonor(@PathVariable Long id) {
        donorService.deleteDonor(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{donorId}/sponsorships")
    public ResponseEntity<List<SponsorshipResponseDto>> getSponsorshipsByDonorId(
            @PathVariable Long donorId) {
        return ResponseEntity.ok(sponsorshipService.getSponsorshipsByDonorId(donorId));
    }

//    @GetMapping("/{donorId}/sponsorships/status/{status}")
//    public ResponseEntity<List<SponsorshipResponseDto>> getSponsorshipsByDonorIdAndStatus(
//            @PathVariable Long donorId,
//            @PathVariable SponsorshipStatus status) {
//        return ResponseEntity.ok(sponsorshipService.getSponsorshipsByDonorIdAndStatus(donorId, status));
//    }

    @GetMapping("/{donorId}/sponsorships/paged")
    public ResponseEntity<Page<SponsorshipResponseDto>> getSponsorshipsByDonorIdWithPagination(
            @PathVariable Long donorId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "startDate") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {

        Sort sort = sortDir.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(page, size, sort);
        return ResponseEntity.ok(sponsorshipService.getSponsorshipsByDonorId(donorId, pageable));
    }




}
