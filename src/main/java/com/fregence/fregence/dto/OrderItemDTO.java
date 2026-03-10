package com.fregence.fregence.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderItemDTO {
    private Long id;
    private Long perfumeId;
    private String perfumeName;
    private String brand; // Bura hələlik null göndəririk, eybi yoxdur
    private Double price;
    private Integer quantity;
    private Double subTotal;
}