package com.fregence.fregence.dto;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class OrderResponseDTO {
    private Long id;
    private String customerName;  // Müştərinin adı
    private String customerEmail; // Müştərinin emaili
    private Double totalAmount;
    private String address;
    private String phoneNumber;
    private String orderNote;
    private String status;
    private LocalDateTime orderDate;
    private LocalDateTime preferredDeliveryTime;
    private List<OrderItemDTO> items;
}