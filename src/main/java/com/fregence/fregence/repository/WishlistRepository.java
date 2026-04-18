package com.fregence.fregence.repository;

import java.util.List;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;

import com.fregence.fregence.entity.Perfume;
import com.fregence.fregence.entity.User;
import com.fregence.fregence.entity.Wishlist;

import jakarta.transaction.Transactional;

@Repository
public interface WishlistRepository extends JpaRepository<Wishlist, Long> {
    List<Wishlist> findByUser(User user);
    long countByUser(User user);
    // Eyni ətir siyahıda artıq varmı? (Təkrarın qarşısını almaq üçün)
    boolean existsByUserAndPerfume(User user, Perfume perfume);
    
    // Silmək üçün
    void deleteByUserAndPerfume(User user, Perfume perfume);
    
    @Modifying
    @Transactional
    void deleteByPerfume(Perfume perfume);
}