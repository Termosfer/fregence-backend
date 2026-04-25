package com.fregence.fregence.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import com.fregence.fregence.dto.ChangePasswordDTO;
import com.fregence.fregence.dto.UserResponseDTO;
import com.fregence.fregence.dto.UserUpdateDTO;
import com.fregence.fregence.entity.User;
import com.fregence.fregence.service.UserService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;
    private final PasswordEncoder passwordEncoder;

    public UserController(UserService userService, PasswordEncoder passwordEncoder) {
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
    }

    // 1. Öz profilini görmək
    @GetMapping("/me")
    public ResponseEntity<UserResponseDTO> getMyProfile() {
        User user = userService.getCurrentUser();
        return ResponseEntity.ok(new UserResponseDTO(
            user.getId(), user.getName(), user.getEmail(), user.getRole().name()
        ));
    }

    // 2. Profili yeniləmək (Ad/Email)
    @PutMapping("/me")
    public ResponseEntity<UserResponseDTO> updateMyProfile(@Valid @RequestBody UserUpdateDTO updateDTO) {
        User updated = userService.updateProfile(updateDTO);
        return ResponseEntity.ok(new UserResponseDTO(
            updated.getId(), updated.getName(), updated.getEmail(), updated.getRole().name()
        ));
    }

    // 3. Şifrəni dəyişmək
    @PatchMapping("/me/password")
    public ResponseEntity<String> changePassword(@Valid @RequestBody ChangePasswordDTO passwordDTO) {
        userService.changePassword(passwordDTO, passwordEncoder);
        return ResponseEntity.ok("Şifrə uğurla dəyişdirildi.");
    }
    @GetMapping("/admin/all-users")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<UserResponseDTO>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }
}