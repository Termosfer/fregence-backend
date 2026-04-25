package com.fregence.fregence.service;

import java.time.LocalDateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.fregence.fregence.dto.DashboardDTO;
import com.fregence.fregence.repository.*;

@Service
public class DashboardService {

    @Autowired private UserRepository userRepository;
    @Autowired private OrderRepository orderRepository;
    @Autowired private PerfumeRepository perfumeRepository;
    @Autowired private OrderItemRepository orderItemRepository;

    public DashboardDTO getDashboardStats() {
        DashboardDTO dto = new DashboardDTO();
        
        // 1. Təməl rəqəmlər
        dto.setTotalUsers(userRepository.count());
        dto.setTotalOrders(orderRepository.count());
        dto.setTotalPerfumes(perfumeRepository.count());
        
        // 2. Real Qazanc (Yalnız DELIVERED statusunda olanlar)
        Double revenue = orderRepository.getTotalDeliveredRevenue();
        dto.setTotalRevenue(revenue != null ? revenue : 0.0);
        
        // 3. Ortalama Sifariş Məbləği (AOV)
        if (dto.getTotalOrders() > 0) {
            double aov = dto.getTotalRevenue() / dto.getTotalOrders();
            dto.setAverageOrderValue(Math.round(aov * 100.0) / 100.0); // 120.45 kimi yuvarlaqlaşdırır
        }
        
        // 4. Müştəri Artım Faizini hesablayırıq
        dto.setCustomerGrowthRate(calculateGrowth());
        
        // 5. Ən çox satılan ətir
        String topPerfume = orderItemRepository.getTopSellingPerfumeName();
        dto.setTopSellingPerfume(topPerfume != null ? topPerfume : "Məlumat yoxdur");
        
        return dto;
    }

    private double calculateGrowth() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startOfThisMonth = now.withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0);
        LocalDateTime startOfLastMonth = startOfThisMonth.minusMonths(1);

        // Bu ay gələn istifadəçilər
        long currentMonthCount = userRepository.countByCreatedAtAfter(startOfThisMonth);
        // Keçən ay gələn istifadəçilər
        long lastMonthCount = userRepository.countByCreatedAtBetween(startOfLastMonth, startOfThisMonth);

        if (lastMonthCount == 0) return currentMonthCount > 0 ? 100.0 : 0.0;

        double growth = ((double)(currentMonthCount - lastMonthCount) / lastMonthCount) * 100;
        return Math.round(growth * 10.0) / 10.0; // 15.5% kimi göstərir
    }
}