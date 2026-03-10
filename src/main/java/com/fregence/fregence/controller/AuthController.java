package com.fregence.fregence.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fregence.fregence.dto.LoginRequest; // <--- Yeni DTO
import com.fregence.fregence.dto.LoginResponse;
import com.fregence.fregence.dto.UserResponseDTO;
import com.fregence.fregence.entity.User;
import com.fregence.fregence.security.JwtUtil;
import com.fregence.fregence.service.UserService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private UserService userService;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtUtil jwtUtil;

    @PostMapping("/register")
    public UserResponseDTO register(@Valid @RequestBody User user) {
        User savedUser = userService.register(user);
        // Entity-ni DTO-ya çeviririk
        return new UserResponseDTO(
            savedUser.getId(), 
            savedUser.getName(), 
            savedUser.getEmail(), 
            savedUser.getRole().name()
        );
    }

    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest request) { // User yerinə LoginRequest
        
        // 1. Şifrə və email doğruluğunu yoxlayır (Səhv olsa BadCredentialsException atır)
        authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(
                request.getEmail(),
                request.getPassword()
            )
        );

        // 2. İstifadəçi məlumatlarını bazadan gətiririk
        User user = userService.findByEmail(request.getEmail());
        
        // 3. Token yaradırıq
        String token = jwtUtil.generateToken(user.getEmail());

        // 4. LoginResponse qaytarırıq (Parametrlərin ardıcıllığına diqqət!)
        // Constructor: (token, name, email, role)
        return new LoginResponse(
            token, 
            user.getName(), 
            user.getEmail(), 
            user.getRole().name() // Enum-u String-ə çeviririk
        );
    }
}