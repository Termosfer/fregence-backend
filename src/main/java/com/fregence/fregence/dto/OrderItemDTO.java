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
    private Long variantId;  // Hansı variant olduğunu bilmək üçün
    private String perfumeName;
    private String brand;    // Null olsa belə, sahənin olması yaxşıdır
    private Integer ml;      // VACİB: Müştəri nə aldığını (məs: 50ml) görməlidir
    private Double price;    // Alış anındakı qiymət
    private Integer quantity;
    private Double subTotal;
    private String imageUrl;
}