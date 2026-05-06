package com.fregence.fregence.repository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.fregence.fregence.entity.CartItem;
import com.fregence.fregence.entity.Perfume;

import io.lettuce.core.dynamic.annotation.Param;
import jakarta.transaction.Transactional;
@Repository
public interface CartItemRepository extends JpaRepository<CartItem, Long> {
	 @Modifying
	    @Transactional
	    // VACİB: Artıq 'perfumeVariant' üzərindən 'perfume'a çatırıq
	    @Query("DELETE FROM CartItem c WHERE c.perfumeVariant.perfume = :perfume")
	    void deleteByPerfume(@Param("perfume") Perfume perfume);
}