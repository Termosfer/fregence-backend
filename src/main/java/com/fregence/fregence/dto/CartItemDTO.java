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
	private String perfumeName;
	private String brand;
	private Double price; // Endirim varsa endirimli qiymət
	private Integer quantity;
	private Double subTotal; // price * quantity
}