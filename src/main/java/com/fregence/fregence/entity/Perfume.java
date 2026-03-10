package com.fregence.fregence.entity;

import java.time.LocalDateTime;
import jakarta.persistence.*;
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
    private String imagePublicId; // Cloudinary-dən şəkli silmək üçün lazım olan ID
    private String brand;
    private String name;
    private String description;
    private String imageUrl;

    private Double price;
    
    @Column(name = "discount_price")
    private Double discountPrice;

    private Integer ml; // Məs: 50, 100

    @Enumerated(EnumType.STRING)
    private Gender gender; // Kişi, Qadın, Unisex

    private Integer stock; // Stok sayı

    private Boolean isNew = true; // Yeni məhsuldurmu?
    private Boolean isRecommended = false;
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    // LOMBOK (@Data) istifadə etdiyin üçün manual Getter/Setter-lərə ehtiyac yoxdur!
    // Onları silsən kodun daha təmiz olar.
}