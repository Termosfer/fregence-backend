package com.fregence.fregence.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "cart_items")
@Data
public class CartItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "cart_id")
    private Cart cart;

 // KÖHNƏ: private Perfume perfume;
    // YENİ: Artıq variantı saxlayırıq (qiymət bunun içindədir)
    @ManyToOne
    @JoinColumn(name = "variant_id")
    private PerfumeVariant perfumeVariant; 

    private Integer quantity; // Məhsulun sayı
}