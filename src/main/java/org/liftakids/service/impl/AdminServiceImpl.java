package org.liftakids.service.impl;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.liftakids.dto.admin.*;
import org.liftakids.entity.SystemAdmin;
import org.liftakids.repositories.AdminRepository;
import org.liftakids.repositories.InstitutionRepository;
import org.liftakids.service.AdminService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminServiceImpl implements AdminService {

    private final AdminRepository adminRepository;
    private final InstitutionRepository institutionRepository;
   // private final PasswordEncoder passwordEncoder;

    @Override
    public AuthResponseDTO login(AdminLoginDTO loginDTO) {
        try {
            SystemAdmin admin = adminRepository.findByUsername(loginDTO.getUsername())
                    .orElseThrow(() -> new RuntimeException("Admin not found"));

            if (!admin.isActive()) {
                throw new RuntimeException("Admin account is deactivated");
            }

            // Temporarily use plain text password comparison
            if (!admin.getPassword().equals(loginDTO.getPassword())) {
                // if (!passwordEncoder.matches(loginDTO.getPassword(), admin.getPassword())) {
                throw new RuntimeException("Invalid password");
            }

            // In real app, generate JWT token here
            String token = generateToken(admin);

            return AuthResponseDTO.builder()
                    .success(true)
                    .message("Login successful")
                    .token(token)
                    .admin(convertToResponseDTO(admin))
                    .build();

        } catch (Exception e) {
            log.error("Login failed: {}", e.getMessage());
            return AuthResponseDTO.builder()
                    .success(false)
                    .message(e.getMessage())
                    .build();
        }
    }

    @Override
    @Transactional
    public AdminResponseDTO createFirstAdmin(AdminDTO adminDTO) {
        if (adminRepository.count() > 0) {
            throw new RuntimeException("First admin already exists. Use regular create endpoint.");
        }

        return createAdminInternal(adminDTO, null, true);
    }

    @Override
    @Transactional
    public AdminResponseDTO createAdminByAdmin(AdminDTO adminDTO, Long createdByAdminId) {
        // Verify creator admin exists and is active
        SystemAdmin creator = adminRepository.findById(createdByAdminId)
                .orElseThrow(() -> new RuntimeException("Creator admin not found"));

        if (!creator.isActive()) {
            throw new RuntimeException("Creator admin is not active");
        }

        return createAdminInternal(adminDTO, createdByAdminId, false);
    }

    private AdminResponseDTO createAdminInternal(AdminDTO adminDTO, Long createdByAdminId, boolean isFirstAdmin) {
        // Check if username exists
        if (adminRepository.existsByUsername(adminDTO.getUsername())) {
            throw new RuntimeException("Username already exists");
        }

        // Check if email exists
        if (adminRepository.existsByEmail(adminDTO.getEmail())) {
            throw new RuntimeException("Email already exists");
        }

        SystemAdmin admin = new SystemAdmin();
        admin.setName(adminDTO.getName());
        admin.setEmail(adminDTO.getEmail());
        admin.setUsername(adminDTO.getUsername());
        admin.setPassword(adminDTO.getPassword()); // Plain text for dev

        // First admin gets super admin privileges
        // Simple logging
        if (createdByAdminId == null) {
            log.info("First admin created: {}", adminDTO.getUsername());
        } else {
            log.info("Admin created: {} by Admin ID: {}", adminDTO.getUsername(), createdByAdminId);
        }
//        if (isFirstAdmin) {
//            admin.setRole("SUPER_ADMIN");
//            admin.setActive(true);
//            log.info("🎉 First admin (Super Admin) created: {}", adminDTO.getUsername());
//        } else {
//            admin.setRole("ADMIN");
//            admin.setActive(true);
//            log.info("Admin created: {} by Admin ID: {}", adminDTO.getUsername(), createdByAdminId);
//        }

        SystemAdmin savedAdmin = adminRepository.save(admin);
        return convertToResponseDTO(savedAdmin);
    }
//    private String processPassword(String rawPassword) {
//        if (encodePassword && passwordEncoder != null) {
//            return passwordEncoder.encode(rawPassword);
//        }
//        return rawPassword; // Plain text in development
//    }


    @Override
    @Transactional
    public AdminResponseDTO updateAdmin(Long adminId, AdminDTO adminDTO, Long updatedByAdminId) {
        SystemAdmin admin = adminRepository.findById(adminId)
                .orElseThrow(() -> new EntityNotFoundException("Admin not found with ID: " + adminId));

        // Check if email is being changed and if new email exists
        if (!admin.getEmail().equals(adminDTO.getEmail())) {
            if (adminRepository.existsByEmail(adminDTO.getEmail())) {
                throw new RuntimeException("Email already exists");
            }
            admin.setEmail(adminDTO.getEmail());
        }

        // Check if username is being changed and if new username exists
        if (!admin.getUsername().equals(adminDTO.getUsername())) {
            if (adminRepository.existsByUsername(adminDTO.getUsername())) {
                throw new RuntimeException("Username already exists");
            }
            admin.setUsername(adminDTO.getUsername());
        }
        if (adminDTO.getPassword() != null && !adminDTO.getPassword().trim().isEmpty()) {
            admin.setPassword(adminDTO.getPassword()); // Plain text
        }
//        admin.setName(adminDTO.getName());
//        if (adminDTO.getPassword() != null && !adminDTO.getPassword().trim().isEmpty()) {
//            admin.setPassword(processPassword(adminDTO.getPassword()));
//        }
        // Only update password if provided
//        if (adminDTO.getPassword() != null && !adminDTO.getPassword().trim().isEmpty()) {
//            admin.setPassword(passwordEncoder.encode(adminDTO.getPassword()));
//        }

        SystemAdmin updatedAdmin = adminRepository.save(admin);

        log.info("Admin updated: {} by Admin ID: {}", adminId, updatedByAdminId);
        return convertToResponseDTO(updatedAdmin);
    }

    @Override
    @Transactional
    public void deleteAdmin(Long adminId, Long deletedByAdminId) {
        SystemAdmin admin = adminRepository.findById(adminId)
                .orElseThrow(() -> new EntityNotFoundException("Admin not found with ID: " + adminId));

        // Prevent deleting self
        if (adminId.equals(deletedByAdminId)) {
            throw new RuntimeException("Cannot delete your own account");
        }

        // Check if admin has approved institutions
        if (admin.getApprovedInstitutions() != null && !admin.getApprovedInstitutions().isEmpty()) {
            throw new RuntimeException("Cannot delete admin who has approved institutions");
        }

        adminRepository.delete(admin);
        log.info("Admin deleted: {} by Admin ID: {}", adminId, deletedByAdminId);
    }

    @Override
    @Transactional
    public void deactivateAdmin(Long adminId, Long deactivatedByAdminId) {
        SystemAdmin admin = adminRepository.findById(adminId)
                .orElseThrow(() -> new EntityNotFoundException("Admin not found with ID: " + adminId));

        // Prevent deactivating self
        if (adminId.equals(deactivatedByAdminId)) {
            throw new RuntimeException("Cannot deactivate your own account");
        }

        admin.setActive(false);
        adminRepository.save(admin);

        log.info("Admin deactivated: {} by Admin ID: {}", adminId, deactivatedByAdminId);
    }

    @Override
    @Transactional
    public void activateAdmin(Long adminId, Long activatedByAdminId) {
        SystemAdmin admin = adminRepository.findById(adminId)
                .orElseThrow(() -> new EntityNotFoundException("Admin not found with ID: " + adminId));

        admin.setActive(true);
        adminRepository.save(admin);

        log.info("Admin activated: {} by Admin ID: {}", adminId, activatedByAdminId);
    }

    @Override
    public AdminResponseDTO getAdminById(Long adminId) {
        SystemAdmin admin = adminRepository.findById(adminId)
                .orElseThrow(() -> new EntityNotFoundException("Admin not found with ID: " + adminId));
        return convertToResponseDTO(admin);
    }

    @Override
    public AdminResponseDTO getAdminByUsername(String username) {
        SystemAdmin admin = adminRepository.findByUsername(username)
                .orElseThrow(() -> new EntityNotFoundException("Admin not found with username: " + username));
        return convertToResponseDTO(admin);
    }

    @Override
    public List<AdminResponseDTO> getAllAdmins() {
        return adminRepository.findAll().stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<AdminResponseDTO> getActiveAdmins() {
        return adminRepository.findByActiveTrue().stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<AdminResponseDTO> getInactiveAdmins() {
        return adminRepository.findByActiveFalse().stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void changePassword(Long adminId, ChangePasswordDTO changePasswordDTO) {
        SystemAdmin admin = adminRepository.findById(adminId)
                .orElseThrow(() -> new EntityNotFoundException("Admin not found"));

        // Verify current password (plain text comparison)
        if (!admin.getPassword().equals(changePasswordDTO.getCurrentPassword())) {
            // if (!passwordEncoder.matches(changePasswordDTO.getCurrentPassword(), admin.getPassword())) {
            throw new RuntimeException("Current password is incorrect");
        }

        // Check if new password matches confirmation
        if (!changePasswordDTO.getNewPassword().equals(changePasswordDTO.getConfirmPassword())) {
            throw new RuntimeException("New password and confirmation do not match");
        }

        // Update password (plain text)
        admin.setPassword(changePasswordDTO.getNewPassword());
        // admin.setPassword(passwordEncoder.encode(changePasswordDTO.getNewPassword()));

        adminRepository.save(admin);

        log.info("Password changed for admin ID: {}", adminId);
    }

    @Override
    @Transactional
    public void resetPassword(Long adminId, Long resetByAdminId) {
        SystemAdmin admin = adminRepository.findById(adminId)
                .orElseThrow(() -> new EntityNotFoundException("Admin not found"));

        // Generate temporary password (plain text)
        String tempPassword = generateTemporaryPassword();
        admin.setPassword(tempPassword);
        // admin.setPassword(passwordEncoder.encode(tempPassword));

        adminRepository.save(admin);

        log.info("Password reset for admin ID: {} by Admin ID: {}", adminId, resetByAdminId);
        // In real app, send email with temp password
    }

    @Override
    public AdminResponseDTO updateProfile(Long adminId, AdminDTO adminDTO) {
        return updateAdmin(adminId, adminDTO, adminId); // Self-update
    }

    @Override
    public SystemAdmin getCurrentAdmin() {
        // Implement based on your authentication mechanism
        // This should get admin from SecurityContext
        return null; // Placeholder
    }

    // Helper methods
    private AdminResponseDTO convertToResponseDTO(SystemAdmin admin) {
        AdminResponseDTO dto = new AdminResponseDTO();
        dto.setAdminId(admin.getAdminId());
        dto.setName(admin.getName());
        dto.setEmail(admin.getEmail());
        dto.setUsername(admin.getUsername());
        dto.setActive(admin.isActive());
        dto.setApprovedInstitutionsCount(
                admin.getApprovedInstitutions() != null ?
                        admin.getApprovedInstitutions().size() : 0
        );
        return dto;
    }

    private String generateToken(SystemAdmin admin) {
        // Implement JWT token generation
        // For now, return a simple token
        return "admin-token-" + admin.getAdminId() + "-" + System.currentTimeMillis();
    }

    private String generateTemporaryPassword() {
        return "Temp@" + System.currentTimeMillis() % 10000;
    }




}
