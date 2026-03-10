package com.fregence.fregence.repository;



import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.fregence.fregence.entity.Subscriber;

public interface SubscriberRepository extends JpaRepository<Subscriber, Long> {
    // Email-ə görə axtarış və səhifələmə
    Page<Subscriber> findByEmailContainingIgnoreCase(String email, Pageable pageable);
	boolean existsByEmail(String email);
}
