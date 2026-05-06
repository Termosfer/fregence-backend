package com.fregence.fregence.entity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "perfumes")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Perfume {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String imagePublicId;
    private String brand;
    private String name;
    private String description;
    private String imageUrl;

    // Qiymətlər artıq Variant cədvəlində olacaq, 
    // Amma 'price' sahəsini "X AZN-dən başlayan qiymətlərlə" 
    // kimi göstərmək üçün ən kiçik qiymət olaraq burada saxlaya bilərsən.
    private Double price; 

    @Enumerated(EnumType.STRING)
    private Gender gender;

    private Boolean isNew = true;
    private Boolean isRecommended = false;
    private LocalDateTime createdAt;

    // YENİ: Variantlar siyahısı
    @OneToMany(mappedBy = "perfume", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<PerfumeVariant> variants = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}