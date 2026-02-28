package org.liftakids.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.liftakids.dto.admin.*;
import org.liftakids.repositories.AdminRepository;
import org.liftakids.service.AdminService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;
    private final AdminRepository adminRepository;
    // Authentication
    @PostMapping("/login")
    public ResponseEntity<AuthResponseDTO> login(@Valid @RequestBody AdminLoginDTO loginDTO) {
        AuthResponseDTO response = adminService.login(loginDTO);
        return ResponseEntity.ok(response);
    }

    // CRUD Operations
    @PostMapping("/first")
    public ResponseEntity<AdminResponseDTO> createFirstAdmin(
            @Valid @RequestBody AdminDTO adminDTO) {

        // Check if any admin already exists
        if (adminRepository.count() > 0) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(null);
        }

        AdminResponseDTO response = adminService.createFirstAdmin(adminDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // Create admin by existing admin (requires authentication)
    @PostMapping
    public ResponseEntity<AdminResponseDTO> createAdmin(
            @Valid @RequestBody AdminDTO adminDTO,
            @RequestHeader("X-Admin-Id") Long adminId) {

        AdminResponseDTO response = adminService.createAdminByAdmin(adminDTO, adminId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{adminId}")
    public ResponseEntity<AdminResponseDTO> updateAdmin(
            @PathVariable Long adminId,
            @Valid @RequestBody AdminDTO adminDTO,
            @RequestHeader("X-Admin-Id") Long updatedByAdminId) {
        AdminResponseDTO response = adminService.updateAdmin(adminId, adminDTO, updatedByAdminId);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{adminId}")
    public ResponseEntity<Void> deleteAdmin(
            @PathVariable Long adminId,
            @RequestHeader("X-Admin-Id") Long deletedByAdminId) {
        adminService.deleteAdmin(adminId, deletedByAdminId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{adminId}/deactivate")
    public ResponseEntity<Void> deactivateAdmin(
            @PathVariable Long adminId,
            @RequestHeader("X-Admin-Id") Long deactivatedByAdminId) {
        adminService.deactivateAdmin(adminId, deactivatedByAdminId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{adminId}/activate")
    public ResponseEntity<Void> activateAdmin(
            @PathVariable Long adminId,
            @RequestHeader("X-Admin-Id") Long activatedByAdminId) {
        adminService.activateAdmin(adminId, activatedByAdminId);
        return ResponseEntity.ok().build();
    }

    // Read Operations
    @GetMapping("/{adminId}")
    public ResponseEntity<AdminResponseDTO> getAdminById(@PathVariable Long adminId) {
        AdminResponseDTO admin = adminService.getAdminById(adminId);
        return ResponseEntity.ok(admin);
    }

    @GetMapping("/username/{username}")
    public ResponseEntity<AdminResponseDTO> getAdminByUsername(@PathVariable String username) {
        AdminResponseDTO admin = adminService.getAdminByUsername(username);
        return ResponseEntity.ok(admin);
    }

    @GetMapping
    public ResponseEntity<List<AdminResponseDTO>> getAllAdmins() {
        List<AdminResponseDTO> admins = adminService.getAllAdmins();
        return ResponseEntity.ok(admins);
    }

    @GetMapping("/active")
    public ResponseEntity<List<AdminResponseDTO>> getActiveAdmins() {
        List<AdminResponseDTO> admins = adminService.getActiveAdmins();
        return ResponseEntity.ok(admins);
    }

    @GetMapping("/inactive")
    public ResponseEntity<List<AdminResponseDTO>> getInactiveAdmins() {
        List<AdminResponseDTO> admins = adminService.getInactiveAdmins();
        return ResponseEntity.ok(admins);
    }

    // Password Operations
    @PostMapping("/{adminId}/change-password")
    public ResponseEntity<Void> changePassword(
            @PathVariable Long adminId,
            @Valid @RequestBody ChangePasswordDTO changePasswordDTO) {
        adminService.changePassword(adminId, changePasswordDTO);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{adminId}/reset-password")
    public ResponseEntity<Void> resetPassword(
            @PathVariable Long adminId,
            @RequestHeader("X-Admin-Id") Long resetByAdminId) {
        adminService.resetPassword(adminId, resetByAdminId);
        return ResponseEntity.ok().build();
    }

    // Profile Operations
    @PutMapping("/profile/{adminId}")
    public ResponseEntity<AdminResponseDTO> updateProfile(
            @PathVariable Long adminId,
            @Valid @RequestBody AdminDTO adminDTO) {
        AdminResponseDTO response = adminService.updateProfile(adminId, adminDTO);
        return ResponseEntity.ok(response);
    }

}
