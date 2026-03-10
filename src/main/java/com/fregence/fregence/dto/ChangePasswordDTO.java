package com.fregence.fregence.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ChangePasswordDTO {
    @NotBlank(message = "Köhnə şifrəni yazın")
    private String oldPassword;

    @NotBlank(message = "Yeni şifrəni yazın")
    @Size(min = 6, message = "Yeni şifrə ən az 6 simvol olmalıdır")
    private String newPassword;
}