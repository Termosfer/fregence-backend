package com.fregence.fregence.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.fregence.fregence.entity.User;

@Repository
public interface  UserRepository extends JpaRepository<User, Long> {
Optional<User>findByEmail(String email);
boolean existsByEmail(String email);
long countByCreatedAtAfter(java.time.LocalDateTime date);
long countByCreatedAtBetween(java.time.LocalDateTime start, java.time.LocalDateTime end);
}
