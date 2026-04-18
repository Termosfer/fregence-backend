package com.fregence.fregence.repository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;

import com.fregence.fregence.entity.CartItem;
import com.fregence.fregence.entity.Perfume;

import jakarta.transaction.Transactional;
@Repository
public interface CartItemRepository extends JpaRepository<CartItem, Long> {
	@Modifying
	@Transactional
	void deleteByPerfume(Perfume perfume);
}