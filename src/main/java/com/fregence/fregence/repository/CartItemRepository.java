package com.fregence.fregence.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param; // DÜZƏLİŞ: Spring Data olmalıdır
import org.springframework.stereotype.Repository;

import com.fregence.fregence.entity.Cart;
import com.fregence.fregence.entity.CartItem;
import com.fregence.fregence.entity.Perfume;
import com.fregence.fregence.entity.PerfumeVariant;

import jakarta.transaction.Transactional;

@Repository
public interface CartItemRepository extends JpaRepository<CartItem, Long> {

    // 1. Səbətdə artıq bu ölçüdə ətrin olub-olmadığını tapmaq üçün (CartService-də istifadə edəcəyik)
    Optional<CartItem> findByCartAndPerfumeVariant(Cart cart, PerfumeVariant perfumeVariant);

    @Modifying
    @Transactional
    // VACİB: Artıq 'perfumeVariant' üzərindən 'perfume'a çatırıq
    @Query("DELETE FROM CartItem c WHERE c.perfumeVariant.perfume = :perfume")
    void deleteByPerfume(@Param("perfume") Perfume perfume);

    // 2. Səbəti tam təmizləmək üçün
    @Modifying
    @Transactional
    void deleteByCart(Cart cart);
}