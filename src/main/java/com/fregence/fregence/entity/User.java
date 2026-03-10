package com.fregence.fregence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "users")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class User {
	@Enumerated(EnumType.STRING)
	private Role role;
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	
	@Column(unique = true, nullable = false)
	@Email(message = "Düzgün email formatı daxil edin")
	@NotBlank(message = "Email boş ola bilməz")
	private String email;

	@Column(nullable = false)
	@Size(min = 6, message = "Şifrə ən az 6 simvol olmalıdır")
	private String password;
	
	@NotBlank(message = "Ad boş ola bilməz")
	private String name;
}
