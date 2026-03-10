package com.fregence.fregence.service;

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
        
        dto.setTotalUsers(userRepository.count());
        dto.setTotalOrders(orderRepository.count());
        dto.setTotalPerfumes(perfumeRepository.count());
        
        Double revenue = orderRepository.getTotalRevenue();
        dto.setTotalRevenue(revenue != null ? revenue : 0.0);
        
        String topPerfume = orderItemRepository.getTopSellingPerfumeName();
        dto.setTopSellingPerfume(topPerfume != null ? topPerfume : "Hələ yoxdur");
        
        return dto;
    }
}