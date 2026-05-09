package com.fregence.fregence.repository;

import java.util.List;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.fregence.fregence.entity.Perfume;
import com.fregence.fregence.entity.PerfumeVariant;
import com.fregence.fregence.entity.User;
import com.fregence.fregence.entity.Wishlist;

import io.lettuce.core.dynamic.annotation.Param;
import jakarta.transaction.Transactional;

@Repository
public interface WishlistRepository extends JpaRepository<Wishlist, Long> {
    List<Wishlist> findByUser(User user);
    boolean existsByUserAndVariant(User user, PerfumeVariant variant);
    void deleteByUserAndVariant(User user, PerfumeVariant variant);
    long countByUser(User user);
    @Modifying
    @Transactional
    void deleteByVariant(PerfumeVariant variant);
    
    @Modifying
    @Transactional
    @Query("DELETE FROM Wishlist w WHERE w.variant.perfume = :perfume")
    void deleteByPerfume(@Param("perfume") Perfume perfume);
    
    @Modifying
    @Transactional
    void deleteByUser(User user); // İstifadəçi silinəndə wishlist-ini təmizləmək üçün

}