package com.fregence.fregence.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "orders")
@Data
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private User user;

    private Double totalAmount;
    private String address;
    private String phoneNumber;
    private LocalDateTime orderDate;
    private String status; // Məs: "PENDING", "COMPLETED"
    private LocalDateTime preferredDeliveryTime; // Təqvimdən gələn dəqiq tarix və saat
    private String orderNote;  
    
    private String courierName;       // Kuryerin adı
    private String courierPhone;      // Kuryerin əlaqə nömrəsi
    private LocalDateTime estimatedDeliveryTime; // Təxmini çatdırılma vaxtı (Admin tərəfindən təyin olunur)
    
    
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<OrderItem> orderItems = new ArrayList<>();
    
    
    
}