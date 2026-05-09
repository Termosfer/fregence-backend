package com.fregence.fregence.dto;

import java.io.Serializable;
import java.util.List; // Bunu əlavə et
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PerfumeDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;
    private String brand;
    private String name;
    private String imageUrl;
    private String description;
    private String gender;
    private Boolean isNew;
    private Boolean isRecommended;
    
    // ANA SƏHİFƏ ÜÇÜN: "X AZN-dən başlayan" qiyməti göstərmək üçün bunları saxlayırıq
    private Double price;         // Orijinal qiymət
    private Double discountPrice; // Endirimli qiymət (varsa)
    private Integer defaultMl; 
    private Double minPrice; 
    // Ətrin bütün ölçü və qiymət variantları:
    private List<PerfumeVariantDTO> variants;
}