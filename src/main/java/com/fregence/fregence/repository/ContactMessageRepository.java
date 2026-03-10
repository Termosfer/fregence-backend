package com.fregence.fregence.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.fregence.fregence.entity.ContactMessage;

public interface ContactMessageRepository extends JpaRepository<ContactMessage, Long> {
	Page<ContactMessage> findByEmailContainingIgnoreCaseOrNameContainingIgnoreCase(
		    String email,
		    String name,
		    Pageable pageable
		);
}
