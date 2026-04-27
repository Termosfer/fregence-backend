package com.fregence.fregence.security;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class RateLimitingFilter extends OncePerRequestFilter {

    // Hər bir IP üçün ayrı bir "limit qabı" (bucket) saxlayırıq
    private final Map<String, Bucket> cache = new ConcurrentHashMap<>();

    // Limit qaydası: Hər 1 dəqiqədə maksimum 30 sorğu (Ehtiyaca görə dəyişə bilərsən)
    private Bucket createNewBucket() {
        // Yeni Builder strukturu (v8.x+)
        return Bucket.builder()
                .addLimit(Bandwidth.builder()
                        .capacity(25) // Maksimum tutum: 30 sorğu
                        .refillGreedy(25, Duration.ofMinutes(1)) // 1 dəqiqə ərzində 30 "jeton" bərpa olunsun
                        .build())
                .build();
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        // İstifadəçinin IP ünvanını götürürük
        String ip = request.getRemoteAddr();
        
        // Həmin IP üçün bucket yoxdursa yaradırıq, varsa götürürük
        Bucket bucket = cache.computeIfAbsent(ip, k -> createNewBucket());

        // Bir dənə "jeton" işlətməyə çalışırıq
        if (bucket.tryConsume(1)) {
            // İcazə var, növbəti addıma keç
            filterChain.doFilter(request, response);
        } else {
            // Limit dolub!
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.setContentType("application/json");
            response.getWriter().write("{\"error\": \"Limit keçildi\", \"message\": \"Həddindən artıq çox sorğu atdınız. Zəhmət olmasa 1 dəqiqə gözləyin.\"}");
        }
    }
}