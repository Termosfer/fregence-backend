package com.fregence.fregence.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional; // DÜZƏLİŞ

import com.fregence.fregence.entity.Cart;
import com.fregence.fregence.entity.User;

@Repository
public interface CartRepository extends JpaRepository<Cart, Long> {
    
    Optional<Cart> findByUser(User user);

    // VACİB: Silmə əməliyyatı üçün bu iki annotasiya lazımdır
    @Modifying
    @Transactional
    void deleteByUser(User user);
}