package com.fregence.fregence.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.fregence.fregence.entity.Perfume;
import com.fregence.fregence.entity.User;
import com.fregence.fregence.entity.Wishlist;
@Repository
public interface WishlistRepository extends JpaRepository<Wishlist, Long> {
    List<Wishlist> findByUser(User user);
    
    // Eyni ətir siyahıda artıq varmı? (Təkrarın qarşısını almaq üçün)
    boolean existsByUserAndPerfume(User user, Perfume perfume);
    
    // Silmək üçün
    void deleteByUserAndPerfume(User user, Perfume perfume);
}