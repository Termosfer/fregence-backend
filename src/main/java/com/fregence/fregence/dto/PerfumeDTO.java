package com.fregence.fregence.dto;

import lombok.Data;

@Data
public class PerfumeDTO {
    private Long id;
    private String brand;
    private String name;
    private Double price;
    private Double discountPrice;
    private String imageUrl;
    private String description;
    private Integer ml;
    private String gender;
    private Boolean isNew;
    private Boolean isRecommended;
}