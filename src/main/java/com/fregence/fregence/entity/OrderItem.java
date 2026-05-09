package com.fregence.fregence.entity;
import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "order_items")
@Data
public class OrderItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "order_id")
    private Order order;

    @ManyToOne
    @JoinColumn(name = "variant_id") // Perfume əvəzinə Variant-a bağlayırıq
    private PerfumeVariant perfumeVariant;

    private String perfumeName;
    private String brandName; // Brendi də saxlamaq yaxşı olar
    private Integer ml;       // VACİB: 30, 50, 100?
    private Double priceAtPurchase;
    private Integer quantity;
    private String imageUrlAtPurchase;
}