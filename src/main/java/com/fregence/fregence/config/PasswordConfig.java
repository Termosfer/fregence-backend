package com.fregence.fregence.config;

import java.util.Arrays;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import com.fregence.fregence.security.JwtAuthenticationEntryPoint;
import com.fregence.fregence.security.JwtFilter;

@Configuration
public class PasswordConfig {

	private final JwtFilter jwtFilter;
	private final JwtAuthenticationEntryPoint entryPoint;

	public PasswordConfig(JwtFilter jwtFilter, JwtAuthenticationEntryPoint entryPoint) {
		this.jwtFilter = jwtFilter;
		this.entryPoint = entryPoint;
	}

	// PasswordConfig daxilinə əlavə etmək üçün CORS Bean-i
	@Bean
	CorsConfigurationSource corsConfigurationSource() {
		CorsConfiguration configuration = new CorsConfiguration();
		configuration.setAllowedOrigins(Arrays.asList("http://localhost:5173")); // Frontend ünvanın
		configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
		configuration.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type"));
		configuration.setAllowCredentials(true);
		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		source.registerCorsConfiguration("/**", configuration);
		return source;
	}

	@Bean
	SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		http.csrf(csrf -> csrf.disable()).cors(cors -> cors.configurationSource(corsConfigurationSource()))
				// 3. XƏTA İDARƏETMƏSİNİ BURADA AKTİV EDİRİK
				.exceptionHandling(exception -> exception.authenticationEntryPoint(entryPoint))
				// VACİB: JWT üçün sessiyanı söndürün
				.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

				.authorizeHttpRequests(auth -> auth.requestMatchers("/api/users/me", "/api/users/me/**")
						.hasAnyRole("USER", "ADMIN").requestMatchers("/api/cart/**").hasAnyRole("USER", "ADMIN")
						.requestMatchers("/api/orders/**").hasAnyRole("USER", "ADMIN")
						.requestMatchers("/api/orders/admin/**").hasRole("ADMIN")
						.requestMatchers("/api/admin/dashboard/**").hasRole("ADMIN")
						.requestMatchers("/api/orders/checkout").hasAnyRole("USER", "ADMIN")
						.requestMatchers("/api/wishlist/**").hasAnyRole("USER", "ADMIN")
						.requestMatchers("/uploads/**").permitAll()
						// Auth endpoints açıqdır (Login/Register)
						.requestMatchers("/api/auth/**").permitAll().requestMatchers("/error").permitAll()
						.requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()
						// ===== PERFUMES (ƏTİRLƏR) =====
						// GET: Hamı baxa bilsin (Ana yol, slash-li yol və bütün alt yollar)
						.requestMatchers(HttpMethod.GET, "/api/perfumes", "/api/perfumes/", "/api/perfumes/**",
								"/api/perfumes/recommendations", "/api/perfumes/*/related")
						.permitAll()

						// POST, PUT, DELETE: Yalnız ADMIN
						.requestMatchers(HttpMethod.POST, "/api/perfumes", "/api/perfumes/").hasRole("ADMIN")
						.requestMatchers(HttpMethod.PUT, "/api/perfumes/**").hasRole("ADMIN")
						.requestMatchers(HttpMethod.DELETE, "/api/perfumes/**").hasRole("ADMIN")

						// ===== CONTACT =====
						.requestMatchers(HttpMethod.POST, "/api/contact", "/api/contact/").hasAnyRole("USER", "ADMIN")
						.requestMatchers(HttpMethod.GET, "/api/contact", "/api/contact/", "/api/contact/**")
						.hasRole("ADMIN").requestMatchers(HttpMethod.DELETE, "/api/contact/**").hasRole("ADMIN")

						// ===== SUBSCRIBERS =====
						// Login olmuş (USER və ya ADMIN) hər kəs abunə ola bilər
						.requestMatchers(HttpMethod.POST, "/api/subscribers", "/api/subscribers/")
						.hasAnyRole("USER", "ADMIN")

						// GET və DELETE üçün də eyni şəkildə
						.requestMatchers(HttpMethod.GET, "/api/subscribers", "/api/subscribers/").hasRole("ADMIN")
						.requestMatchers(HttpMethod.DELETE, "/api/subscribers/**").hasRole("ADMIN")

						// Digər bütün requestlər login tələb edir
						.anyRequest().authenticated())
				.addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

		return http.build();
	}

	@Bean
	AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
		return authConfig.getAuthenticationManager();
	}

	@Bean
	PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}
}