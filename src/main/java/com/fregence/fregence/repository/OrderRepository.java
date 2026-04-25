package com.fregence.fregence.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.fregence.fregence.entity.Order;
import com.fregence.fregence.entity.User;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByUserOrderByOrderDateDesc(User user); // İstifadəçinin öz sifarişləri
 // Bütün tamamlanmış sifarişlərin cəmini hesablayır
    @Query("SELECT SUM(o.totalAmount) FROM Order o WHERE o.status = 'DELIVERED'")
    Double getTotalDeliveredRevenue();
}