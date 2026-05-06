package com.fregence.fregence.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CartItemDTO {
	 private Long cartItemId;
	    private Long perfumeId;
	    private Long variantId; // Variant ID-si əlavə olundu
	    private String perfumeName;
	    private String brand;
	    private Integer ml;     // Seçilən ölçü (məs: 50ml)
	    private Double price;
	    private Double discountPrice;
	    private Integer quantity;
	    private Double subTotal;
	    private String imageUrl;
}