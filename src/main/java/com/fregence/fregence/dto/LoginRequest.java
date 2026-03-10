package com.fregence.fregence.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public class LoginRequest {

    @NotBlank(message = "Email boş ola bilməz")
    @Email(message = "Düzgün email formatı daxil edin")
    private String email;

    @NotBlank(message = "Şifrə boş ola bilməz")
    // Burada @Size qoymağa ehtiyac yoxdur, çünki login-də formatı deyil, 
    // şifrənin doğruluğunu bazadan yoxlayacağıq.
    private String password;

    // Getters and Setters
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
}