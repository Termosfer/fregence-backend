package com.fregence.fregence.config;

import java.util.Arrays;
import org.springframework.beans.factory.annotation.Autowired;
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
import com.fregence.fregence.security.RateLimitingFilter;

@Configuration
public class PasswordConfig {

    private final JwtFilter jwtFilter;
    private final JwtAuthenticationEntryPoint entryPoint;
    
    @Autowired
    private RateLimitingFilter rateLimitingFilter; 

    public PasswordConfig(JwtFilter jwtFilter, JwtAuthenticationEntryPoint entryPoint) {
        this.jwtFilter = jwtFilter;
        this.entryPoint = entryPoint;
    }

    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList(
            "http://localhost:5173", 
            "http://127.0.0.1:5173", 
            "https://miparfume.netlify.app"
        ));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList(
            "Authorization", "Content-Type", "Accept", "X-Requested-With", 
            "Cache-Control", "Access-Control-Request-Method", "Access-Control-Request-Headers"
        ));
        configuration.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .exceptionHandling(exception -> exception.authenticationEntryPoint(entryPoint))
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                // 1. TAM AÇIQ YOLLAR
                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll() // Preflight sorğuları üçün mütləqdir
                .requestMatchers("/api/auth/**", "/error", "/uploads/**", "/ws-notifications/**").permitAll()
                .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/perfumes/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/perfumes/brands").permitAll()

                // 2. ADMIN YOLLARI
                .requestMatchers("/api/orders/admin/**").hasRole("ADMIN")
                .requestMatchers("/api/admin/dashboard/**").hasRole("ADMIN")
                .requestMatchers("/api/users/admin/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.POST, "/api/perfumes/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.PUT, "/api/perfumes/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/api/perfumes/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.GET, "/api/contact/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/api/contact/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.GET, "/api/subscribers/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/api/subscribers/**").hasRole("ADMIN")

                // 3. USER & ADMIN YOLLARI
                .requestMatchers("/api/users/me/**").hasAnyRole("USER", "ADMIN")
                .requestMatchers("/api/cart/**").hasAnyRole("USER", "ADMIN")
                .requestMatchers("/api/wishlist/**").hasAnyRole("USER", "ADMIN")
                .requestMatchers("/api/orders/my").hasAnyRole("USER", "ADMIN")
                .requestMatchers("/api/orders/checkout").hasAnyRole("USER", "ADMIN")
                .requestMatchers("/api/payment/**").hasAnyRole("USER", "ADMIN")
                .requestMatchers(HttpMethod.POST, "/api/contact/**").hasAnyRole("USER", "ADMIN")
                .requestMatchers(HttpMethod.POST, "/api/subscribers/**").hasAnyRole("USER", "ADMIN")

                .anyRequest().authenticated()
            )
            // VACİB: Filtr sıralaması
            // 1. Əvvəlcə Rate Limit yoxlanılır (Render-i qorumaq üçün)
            .addFilterBefore(rateLimitingFilter, UsernamePasswordAuthenticationFilter.class)
            // 2. Sonra JWT yoxlanılır
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