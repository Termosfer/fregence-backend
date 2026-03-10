package com.fregence.fregence.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;



@Data
@AllArgsConstructor
@NoArgsConstructor
public class DashboardDTO {
    private Long totalUsers;         // Cəmi müştəri sayı
    private Long totalOrders;        // Cəmi sifariş sayı
    private Long totalPerfumes;      // Bazadakı ətir çeşidi sayı
    private Double totalRevenue;     // Ümumi qazanc (AZN)
    private String topSellingPerfume; // Ən çox satılan ətir
}