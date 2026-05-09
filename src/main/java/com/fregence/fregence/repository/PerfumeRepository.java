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

    // 1. Təkmilləşdirilmiş Multi-Filtr (İndi ML (ölçü) də daxildir)
	@Query("SELECT DISTINCT p FROM Perfume p LEFT JOIN p.variants v WHERE " +
		       "(:brand IS NULL OR p.brand ILIKE :brand) AND " + // ILIKE - Case-insensitive axtarış üçün
		       "(:gender IS NULL OR p.gender = :gender) AND " +
		       "(:ml IS NULL OR v.ml = :ml) AND " +
		       "(:minPrice IS NULL OR v.price >= :minPrice) AND " +
		       "(:maxPrice IS NULL OR v.price <= :maxPrice)")
		Page<Perfume> filterPerfumes(
		    @Param("brand") String brand, 
		    @Param("gender") Gender gender, 
		    @Param("minPrice") Double minPrice, 
		    @Param("maxPrice") Double maxPrice, 
		    @Param("ml") Integer ml, 
		    Pageable pageable);

    // 2. Bazadakı bütün unikal ölçüləri (ml) gətirir (Sidebar-da 30, 50, 100 göstərmək üçün)
    @Query("SELECT DISTINCT v.ml FROM PerfumeVariant v ORDER BY v.ml ASC")
    List<Integer> findAllUniqueSizes();

    // 3. Stokda cəmi 3 və ya daha az qalmış variantları olan ətirləri tapır (Flash Sale üçün)
    @Query("SELECT DISTINCT p FROM Perfume p JOIN p.variants v WHERE v.stock > 0 AND v.stock <= 3")
    List<Perfume> findLowStockPerfumes();

    // --- Mövcud metodlarının qorunmuş variantları ---

    Page<Perfume> findByNameContainingIgnoreCaseOrBrandContainingIgnoreCase(String name, String brand, Pageable pageable);

    List<Perfume> findByIsNewTrue();

    List<Perfume> findByIsRecommendedTrue();

    @Query("SELECT DISTINCT p.brand FROM Perfume p ORDER BY p.brand ASC")
    List<String> findUniqueBrands();

    List<Perfume> findTop4ByBrandAndIdNot(String brand, Long id);
}