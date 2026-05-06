package com.fregence.fregence.dto;

import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PerfumeVariantDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;
    private Integer ml;
    private Double price;
    private Double discountPrice;
    private Integer stock;
    
    // Sənin istədiyin marketing sahələri:
    private boolean isLowStock;  // Stok 3 və ya daha azdırsa true olacaq
    private String stockMessage; // "Son 2 ədəd!" kimi mesaj
}