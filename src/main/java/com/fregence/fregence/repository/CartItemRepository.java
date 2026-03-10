package com.fregence.fregence.repository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.fregence.fregence.entity.CartItem;
@Repository
public interface CartItemRepository extends JpaRepository<CartItem, Long> {
}