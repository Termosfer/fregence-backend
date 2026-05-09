package com.fregence.fregence.dto;

import lombok.Data;

@Data
public class WishlistItemDTO {
    private Long wishlistId;
    private Long perfumeId;
    private Long variantId;
    private String brand;
    private String name;
    private String imageUrl;
    private Integer ml;
    private Double price;
    private Double discountPrice;
    private Integer stock;
}
