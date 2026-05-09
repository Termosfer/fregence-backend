package com.fregence.fregence.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import com.fregence.fregence.entity.OrderItem;
import java.util.List;

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {

    @Query("SELECT oi.perfumeName FROM OrderItem oi GROUP BY oi.perfumeName ORDER BY SUM(oi.quantity) DESC LIMIT 1")
    String getTopSellingPerfumeName();

    // DÜZƏLİŞ: Variantın içindəki Perfume-un ID-sinə görə axtarırıq
    List<OrderItem> findByPerfumeVariant_Perfume_Id(Long perfumeId);
}