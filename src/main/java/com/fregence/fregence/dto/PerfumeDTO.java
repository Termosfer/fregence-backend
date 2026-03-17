package com.fregence.fregence.dto;

import java.io.Serializable;

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
    private Double price;
    private Double discountPrice;
    private String imageUrl;
    private String description;
    private Integer ml;
    private String gender;
    private Boolean isNew;
    private Boolean isRecommended;
}