package com.fregence.fregence.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ContactMessageDTO {
	private Long id;
    private String name;
    private String email;
    private String message;
    private LocalDateTime createdAt; // Mesajın nə vaxt gəldiyini görmək Admin üçün vacibdir
}