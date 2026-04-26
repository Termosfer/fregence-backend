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
    @JoinColumn(name = "perfume_id", nullable = true)
    private Perfume perfume;

    private String perfumeName;
    private Double priceAtPurchase;
    private Integer quantity;
    
    // YENİ: Sifariş anındakı şəkil snapshot-u
    private String imageUrlAtPurchase;
}