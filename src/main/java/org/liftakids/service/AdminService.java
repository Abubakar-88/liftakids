package org.liftakids.service;

import org.liftakids.dto.admin.*;
import org.liftakids.entity.SystemAdmin;

import java.util.List;

public interface AdminService {
    // Authentication
    AuthResponseDTO login(AdminLoginDTO loginDTO);
    AdminResponseDTO createFirstAdmin(AdminDTO adminDTO);
    // CRUD Operations
    AdminResponseDTO createAdminByAdmin(AdminDTO adminDTO, Long createdByAdminId);
    AdminResponseDTO updateAdmin(Long adminId, AdminDTO adminDTO, Long updatedByAdminId);
    void deleteAdmin(Long adminId, Long deletedByAdminId);
    void deactivateAdmin(Long adminId, Long deactivatedByAdminId);
    void activateAdmin(Long adminId, Long activatedByAdminId);

    // Read Operations
    AdminResponseDTO getAdminById(Long adminId);
    AdminResponseDTO getAdminByUsername(String username);
    List<AdminResponseDTO> getAllAdmins();
    List<AdminResponseDTO> getActiveAdmins();
    List<AdminResponseDTO> getInactiveAdmins();

    // Password Operations
    void changePassword(Long adminId, ChangePasswordDTO changePasswordDTO);
    void resetPassword(Long adminId, Long resetByAdminId);

    // Profile Operations
    AdminResponseDTO updateProfile(Long adminId, AdminDTO adminDTO);

    // Stats
   // AdminStatsDTO getAdminStats();

    // Get current admin from Security Context
    SystemAdmin getCurrentAdmin();
}