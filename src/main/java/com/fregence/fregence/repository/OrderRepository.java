package com.fregence.fregence.repository;

import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.fregence.fregence.entity.Order;
import com.fregence.fregence.entity.User;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    // 1. Müştəri üçün: Silinməmiş sifarişlərini tarixinə görə gətirir
    List<Order> findByUserAndIsDeletedFalseOrderByOrderDateDesc(User user);
    
    // 2. Admin üçün: Bütün silinməmiş sifarişləri səhifəli (Pagination) gətirir
    Page<Order> findAllByIsDeletedFalse(Pageable pageable);

    // 3. Filtrasiya üçün: Silinməmiş sifarişləri sıralama ilə gətirir
    List<Order> findAllByIsDeletedFalse(Sort sort);

    // 4. İstifadəçi silinəndə onun həm silinmiş, həm də aktiv bütün sifarişlərini tapmaq üçün
    List<Order> findByUser(User user); 

    // 5. Statistika: Çatdırılmış və silinməmiş sifarişlərdən gələn ümumi gəlir
    @Query("SELECT SUM(o.totalAmount) FROM Order o WHERE o.status = 'DELIVERED' AND o.isDeleted = false")
    Double getTotalDeliveredRevenue();
}