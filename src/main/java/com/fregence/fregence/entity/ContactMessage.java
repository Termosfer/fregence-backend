package com.fregence.fregence.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "contact_messages")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ContactMessage {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false)
	@NotBlank(message = "Ad boş ola bilməz")
	private String name;

	@Column(unique = true, nullable = false)
	@NotBlank(message = "Email boş ola bilməz")
	@Email(message = "Zəhmət olmasa düzgün email formatı daxil edin")
	private String email;

	@Column(nullable = false, length = 2000)
	@NotBlank(message = "Mesaj hissəsi boş ola bilməz")
	@Size(min = 10, max = 2000, message = "Mesaj ən az 10, ən çox 2000 simvol olmalıdır")
	private String message;

	private LocalDateTime createdAt;

	@PrePersist
	public void setCreatedAt() {
		this.createdAt = LocalDateTime.now();
	}
}
