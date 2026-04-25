package com.fregence.fregence.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;



@Data
@AllArgsConstructor
@NoArgsConstructor
public class DashboardDTO {
	private long totalUsers;
    private long totalOrders;
    private long totalPerfumes;
    private double totalRevenue;
    
    // XƏTA VERƏN SAHƏLƏR (Bura əlavə olundu):
    private double averageOrderValue;
    private double customerGrowthRate;
    
    private String topSellingPerfume;
}