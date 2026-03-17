package com.fregence.fregence;

import org.springframework.beans.factory.annotation.Value; // Bunu əlavə et
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.scheduling.annotation.EnableAsync;

import com.fregence.fregence.entity.User;
import com.fregence.fregence.entity.Role;
import com.fregence.fregence.repository.UserRepository;

@SpringBootApplication
@EnableAsync
@EnableCaching
public class FregenceApplication {

    // Şifrəni və emaili properties-dən oxuyuruq
	@Value("${APP_ADMIN_EMAIL}")
    private String adminEmail;

	@Value("${APP_ADMIN_PASSWORD}")
    private String adminPassword;

	public static void main(String[] args) {
		SpringApplication.run(FregenceApplication.class, args);
	}

	@Bean
	CommandLineRunner createInitialAdmin(UserRepository userRepository, PasswordEncoder passwordEncoder) {
		return args -> {
			if (userRepository.findByEmail(adminEmail).isEmpty()) {
				User admin = new User();
				admin.setName("Admin");
				admin.setEmail(adminEmail);
				admin.setPassword(passwordEncoder.encode(adminPassword)); // Artıq dəyişəndən gəlir
				admin.setRole(Role.ADMIN);
				
				userRepository.save(admin);
				System.out.println(">>> Sistem: İlk Admin istifadəçisi yaradıldı.");
			}
		};
	}
}