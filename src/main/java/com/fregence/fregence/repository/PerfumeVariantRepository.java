package com.fregence.fregence.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.fregence.fregence.entity.PerfumeVariant;

@Repository
public interface PerfumeVariantRepository extends JpaRepository<PerfumeVariant, Long> {
    // Hələlik standart JpaRepository metodları (findById, save və s.) kifayətdir.
}