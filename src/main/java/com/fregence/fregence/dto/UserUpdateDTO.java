package com.fregence.fregence.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UserUpdateDTO {
    @NotBlank(message = "Ad boş ola bilməz")
    private String name;

    @NotBlank(message = "Email boş ola bilməz")
    @Email(message = "Düzgün email yazın")
    private String email;
}