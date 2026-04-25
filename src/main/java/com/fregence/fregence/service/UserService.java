package com.fregence.fregence.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.fregence.fregence.dto.ChangePasswordDTO;
import com.fregence.fregence.dto.UserResponseDTO;
import com.fregence.fregence.dto.UserUpdateDTO;
import com.fregence.fregence.entity.Role;
import com.fregence.fregence.entity.User;
import com.fregence.fregence.repository.UserRepository;

import jakarta.transaction.Transactional;

import org.springframework.security.core.context.SecurityContextHolder;

@Service
public class UserService {
	
	@Autowired
	private UserRepository userRepository;
	
    @Autowired
	private PasswordEncoder passwordEncoder;
    
    public User register(User user) {
    	if (userRepository.existsByEmail(user.getEmail())) {
            throw new RuntimeException("Email already exists!");
        }

    	// 🔥 DEFAULT ROLE VER
        user.setRole(Role.USER);

        // Hash edib DB-yə göndər
        String rawPassword = user.getPassword();
        user.setPassword(passwordEncoder.encode(rawPassword));
        User savedUser = userRepository.save(user);

        // Cavab üçün plain passwordu geri qoy (optional, təhlükəsizliyə görə tövsiyə edilmir)
        savedUser.setPassword(rawPassword);

        return savedUser;
    }
    public User findByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }
    
 // 1. Hazırda login olan istifadəçini tapmaq
    public User getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("İstifadəçi tapılmadı"));
    }
    
    public List<UserResponseDTO> getAllUsers() {
        return userRepository.findAll().stream()
                .map(user -> new UserResponseDTO(user.getId(), user.getName(), user.getEmail(), user.getRole().name()))
                .toList();
    }

    // 2. Profil məlumatlarını yeniləmək
    @Transactional
    public User updateProfile(UserUpdateDTO updateDTO) {
        User user = getCurrentUser();
        
        // Email dəyişirsə, yeni emailin bazada olub-olmadığını yoxlayaq
        if (!user.getEmail().equals(updateDTO.getEmail()) && 
            userRepository.findByEmail(updateDTO.getEmail()).isPresent()) {
            throw new RuntimeException("Bu email artıq başqa istifadəçi tərəfindən istifadə olunur!");
        }

        user.setName(updateDTO.getName());
        user.setEmail(updateDTO.getEmail());
        return userRepository.save(user);
    }

    // 3. Şifrəni dəyişmək
    @Transactional
    public void changePassword(ChangePasswordDTO passwordDTO, PasswordEncoder passwordEncoder) {
        User user = getCurrentUser();

        // Köhnə şifrənin doğruluğunu yoxlayırıq
        if (!passwordEncoder.matches(passwordDTO.getOldPassword(), user.getPassword())) {
            throw new RuntimeException("Köhnə şifrəniz yanlışdır!");
        }

        // Yeni şifrəni hash-ləyib yadda saxlayırıq
        user.setPassword(passwordEncoder.encode(passwordDTO.getNewPassword()));
        userRepository.save(user);
    }
    
    
}
