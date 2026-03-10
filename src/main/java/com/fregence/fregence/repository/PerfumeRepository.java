package com.fregence.fregence.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.fregence.fregence.entity.Perfume;
import com.fregence.fregence.entity.Gender;

import java.util.List;

public interface PerfumeRepository extends JpaRepository<Perfume, Long> {

    // 1. Ad və ya Brenda görə axtarış (Pagination ilə)
    Page<Perfume> findByNameContainingIgnoreCaseOrBrandContainingIgnoreCase(String name, String brand, Pageable pageable);

    // 2. Cinsə (Gender) görə filtrləmə
    Page<Perfume> findByGender(Gender gender, Pageable pageable);

    // 3. Brendə görə filtrləmə
    Page<Perfume> findByBrandIgnoreCase(String brand, Pageable pageable);

    // 4. Qiymət aralığına görə filtrləmə (Məs: 50 AZN - 200 AZN arası)
    Page<Perfume> findByPriceBetween(Double minPrice, Double maxPrice, Pageable pageable);

    // 5. Yalnız endirimdə olan ətirləri gətir (discountPrice-ı null olmayanlar)
    Page<Perfume> findByDiscountPriceIsNotNull(Pageable pageable);

    // 6. Yeni gələn məhsullar
    List<Perfume> findByIsNewTrue();

    // 7. Professional Bonus: Çoxlu filtr (Eyni anda Brend, Gender və Qiymət yoxlaması üçün)
    @Query("SELECT p FROM Perfume p WHERE " +
           "(:brand IS NULL OR LOWER(p.brand) = LOWER(:brand)) AND " +
           "(:gender IS NULL OR p.gender = :gender) AND " +
           "(:minPrice IS NULL OR p.price >= :minPrice) AND " +
           "(:maxPrice IS NULL OR p.price <= :maxPrice)")
    Page<Perfume> filterPerfumes(@Param("brand") String brand, 
                                 @Param("gender") Gender gender, 
                                 @Param("minPrice") Double minPrice, 
                                 @Param("maxPrice") Double maxPrice, 
                                 Pageable pageable);
    
 // 1. Tövsiyə olunanları gətir (Məsələn: Ana səhifə slaydşousu üçün)
    java.util.List<Perfume> findByIsRecommendedTrue();

    // 2. Oxşar ətirləri gətir (Eyni brenddən olan 4 dənə ətir, amma baxılan ətir özü siyahıda olmasın)
    java.util.List<Perfume> findTop4ByBrandAndIdNot(String brand, Long id);
}