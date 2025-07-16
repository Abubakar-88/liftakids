package org.liftakids.controller;

import lombok.RequiredArgsConstructor;
import org.liftakids.dto.institute.InstitutionRequestDto;
import org.liftakids.dto.institute.InstitutionResponseDto;
import org.liftakids.service.InstitutionService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/institutions")
@RequiredArgsConstructor
public class InstitutionController {

    private final InstitutionService institutionService;

    @PostMapping
    public ResponseEntity<InstitutionResponseDto> createInstitution(@Valid @RequestBody InstitutionRequestDto requestDto) {
        InstitutionResponseDto response = institutionService.createInstitution(requestDto);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<InstitutionResponseDto>> getAllInstitutions() {
        return ResponseEntity.ok(institutionService.getAllInstitutions());
    }

    @GetMapping("/{id}")
    public ResponseEntity<InstitutionResponseDto> getInstitutionById(@PathVariable Long id) {
        return ResponseEntity.ok(institutionService.getInstitutionById(id));
    }
    @PutMapping("/{id}")
    public ResponseEntity<InstitutionResponseDto> updateInstitution(
            @PathVariable Long id,
            @Valid @RequestBody InstitutionRequestDto requestDto) {
        InstitutionResponseDto updated = institutionService.updateInstitution(id, requestDto);
        return ResponseEntity.ok(updated);
    }
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteInstitution(@PathVariable Long id) {
        institutionService.deleteInstitution(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/approved")
    public ResponseEntity<List<InstitutionResponseDto>> getApprovedInstitutions() {
        return ResponseEntity.ok(institutionService.getApprovedInstitutions());
    }
    @GetMapping("/type/{type}")
    public ResponseEntity<List<InstitutionResponseDto>> getByType(@PathVariable String type) {
        return ResponseEntity.ok(institutionService.getInstitutionsByType(type));
    }

    @GetMapping("/search")
    public ResponseEntity<InstitutionResponseDto> getInstitutionByIdOrName(@RequestParam String value) {
        InstitutionResponseDto response = institutionService.getByIdOrName(value);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}/name")
    public ResponseEntity<String> getInstitutionNameById(@PathVariable Long id) {
        String name = institutionService.getInstitutionNameById(id);
        return ResponseEntity.ok(name);
    }


}
