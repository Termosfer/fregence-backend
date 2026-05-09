package com.fregence.fregence.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fregence.fregence.dto.PagedResponse;
import com.fregence.fregence.dto.PerfumeDTO;
import com.fregence.fregence.dto.PerfumeVariantDTO;
import com.fregence.fregence.entity.Perfume;
import com.fregence.fregence.entity.PerfumeVariant;
import com.fregence.fregence.entity.OrderItem;
import com.fregence.fregence.entity.Gender;
import com.fregence.fregence.repository.CartItemRepository;
import com.fregence.fregence.repository.OrderItemRepository;
import com.fregence.fregence.repository.PerfumeRepository;
import com.fregence.fregence.repository.WishlistRepository;

@Service
public class PerfumeService {

	private final PerfumeRepository repository;
	private final FileService fileService;

	@Autowired
	private OrderItemRepository orderItemRepository;
	@Autowired
	private WishlistRepository wishlistRepository;
	@Autowired
	private CartItemRepository cartItemRepository;

	public PerfumeService(PerfumeRepository repository, FileService fileService) {
		this.repository = repository;
		this.fileService = fileService;
	}

	@CacheEvict(value = { "perfumes", "perfume-details", "perfume-related" }, allEntries = true)
	@Transactional
	public PerfumeDTO savePerfume(Perfume perfume) {
		if (perfume.getVariants() != null) {
			perfume.getVariants().forEach(variant -> variant.setPerfume(perfume));
		}
		Perfume savedPerfume = repository.save(perfume);
		return convertToDto(savedPerfume);
	}

	@Cacheable(value = "perfumes", key = "(#query ?: 'default') + '-' + #pageable.pageNumber + '-' + #pageable.pageSize + '-' + #pageable.sort.toString()")
	public PagedResponse<PerfumeDTO> getAllPerfumes(String query, Pageable pageable) {
		Page<Perfume> perfumes;
		if (query != null && !query.isEmpty()) {
			perfumes = repository.findByNameContainingIgnoreCaseOrBrandContainingIgnoreCase(query, query, pageable);
		} else {
			perfumes = repository.findAll(pageable);
		}

		return new PagedResponse<>(perfumes.getContent().stream().map(this::convertToDto).toList(),
				perfumes.getNumber(), perfumes.getSize(), perfumes.getTotalElements(), perfumes.getTotalPages(),
				perfumes.isLast());
	}

	@Cacheable(value = "perfumes", key = "'all-brands'")
	public List<String> getAllBrands() {
		return repository.findUniqueBrands();
	}

	@Cacheable(value = "perfume-details", key = "#id")
	public PerfumeDTO getPerfumeById(Long id) {
		Perfume perfume = repository.findById(id)
				.orElseThrow(() -> new RuntimeException("Perfume not found with id: " + id));
		return convertToDto(perfume);
	}

	@Transactional
	@CacheEvict(value = { "perfumes", "perfume-details", "perfume-related" }, allEntries = true)
	public PerfumeDTO updatePerfume(Long id, Perfume updatedPerfume) {
		// 1. Mövcud datanı bazadan tapırıq
		Perfume existingPerfume = repository.findById(id).orElseThrow(() -> new RuntimeException("Perfume not found"));

		// 2. Sahələri tək-tək yoxlayırıq: YALNIZ NULL DEYİLSƏ YENİLƏYİRİK

		if (updatedPerfume.getName() != null) {
			existingPerfume.setName(updatedPerfume.getName());
		}

		if (updatedPerfume.getBrand() != null) {
			existingPerfume.setBrand(updatedPerfume.getBrand());
		}

		if (updatedPerfume.getDescription() != null) {
			existingPerfume.setDescription(updatedPerfume.getDescription());
		}

		if (updatedPerfume.getGender() != null) {
			existingPerfume.setGender(updatedPerfume.getGender());
		}

		if (updatedPerfume.getIsNew() != null) {
			existingPerfume.setIsNew(updatedPerfume.getIsNew());
		}

		if (updatedPerfume.getIsRecommended() != null) {
			existingPerfume.setIsRecommended(updatedPerfume.getIsRecommended());
		}

		// 3. Şəkil yoxlanışı (Bunu artıq yazmışdıq, qalsın)
		if (updatedPerfume.getImageUrl() != null) {
			if (existingPerfume.getImagePublicId() != null) {
				fileService.deleteImage(existingPerfume.getImagePublicId());
			}
			existingPerfume.setImageUrl(updatedPerfume.getImageUrl());
			existingPerfume.setImagePublicId(updatedPerfume.getImagePublicId());
		}

		// 4. Variantlar yoxlanışı:
		// Əgər JSON-da variants sahəsi göndərilibsə (null deyilsə), onları yeniləyirik
		if (updatedPerfume.getVariants() != null && !updatedPerfume.getVariants().isEmpty()) {
			existingPerfume.getVariants().clear();
			updatedPerfume.getVariants().forEach(v -> {
				v.setPerfume(existingPerfume);
				existingPerfume.getVariants().add(v);
			});
		}

		// 5. Yadda saxlayırıq və DTO-ya çevirib qaytarırıq
		return convertToDto(repository.save(existingPerfume));
	}

	@CacheEvict(value = { "perfumes", "perfume-details", "perfume-related" }, allEntries = true)
@Transactional
public void deletePerfume(Long id) {
    Perfume perfume = repository.findById(id).orElseThrow(() -> new RuntimeException("Perfume not found"));

    // 1. Variant vasitəsilə ətirə bağlı olan OrderItem-ləri tapırıq
    List<OrderItem> orderItems = orderItemRepository.findByPerfumeVariant_Perfume_Id(id);
    if (!orderItems.isEmpty()) {
        // BURADA DÜZƏLİŞ: setPerfume(null) əvəzinə setPerfumeVariant(null) yazırıq
        orderItems.forEach(item -> item.setPerfumeVariant(null));
        orderItemRepository.saveAll(orderItems);
    }

    // 2. Wishlist və CartItem təmizliyi (mövcud kodun davamı)
    wishlistRepository.deleteByPerfume(perfume);
    cartItemRepository.deleteByPerfume(perfume);

    if (perfume.getImagePublicId() != null) {
        fileService.deleteImage(perfume.getImagePublicId());
    }

    repository.delete(perfume);
}

	@Cacheable(value = "perfumes", key = "'filter-' + (#brand ?: 'all') + '-' + (#gender ?: 'all') + '-' + (#ml ?: 'all') + '-' + (#minPrice ?: '0') + '-' + (#maxPrice ?: 'max') + '-' + #pageable.pageNumber + '-' + #pageable.pageSize + '-' + #pageable.sort.toString()")
	public PagedResponse<PerfumeDTO> filterPerfumes(String brand, Gender gender, Integer ml, Double minPrice, Double maxPrice, Pageable pageable) {
	    
	    // Əgər brend gəlibsə, onu % işarələri ilə bükürük
	    String brandParam = (brand != null && !brand.isEmpty()) ? "%" + brand + "%" : null;

	    Page<Perfume> perfumes = repository.filterPerfumes(brandParam, gender, minPrice, maxPrice, ml, pageable);
	    
	    return new PagedResponse<>(
	        perfumes.getContent().stream().map(this::convertToDto).toList(),
	        perfumes.getNumber(),
	        perfumes.getSize(),
	        perfumes.getTotalElements(),
	        perfumes.getTotalPages(),
	        perfumes.isLast()
	    );
	}

	@Cacheable(value = "perfumes", key = "'recommendations'")
	public List<PerfumeDTO> getRecommendedPerfumes() {
		return repository.findByIsRecommendedTrue().stream().map(this::convertToDto).toList();
	}

	@Cacheable(value = "perfume-related", key = "#id")
	public List<PerfumeDTO> getRelatedPerfumes(Long id) {
		Perfume perfume = repository.findById(id).orElseThrow(() -> new RuntimeException("Perfume not found"));
		return repository.findTop4ByBrandAndIdNot(perfume.getBrand(), id).stream().map(this::convertToDto).toList();
	}

	// --- BU METOD MÜTLƏQ OLMALIDIR (Mapping) ---
	public PerfumeDTO convertToDto(Perfume perfume) {
    PerfumeDTO dto = new PerfumeDTO();

    // 1. Əsas məlumatların mənimsədilməsi
    dto.setId(perfume.getId());
    dto.setName(perfume.getName());
    dto.setBrand(perfume.getBrand());
    dto.setDescription(perfume.getDescription());
    dto.setImageUrl(perfume.getImageUrl());
    dto.setIsNew(perfume.getIsNew());
    dto.setIsRecommended(perfume.getIsRecommended());

    if (perfume.getGender() != null) {
        dto.setGender(perfume.getGender().name());
    }

    // 2. Variantların işlənməsi
    if (perfume.getVariants() != null && !perfume.getVariants().isEmpty()) {

        // --- YENİ̇ MƏNTİ̇Q: Ən ucuz variantı tapırıq ---
        // Bu variant həm Ana Səhifədə qiyməti göstərmək üçün, həm də default seçili gəlmək üçün istifadə olunacaq.
        PerfumeVariant cheapestVariant = perfume.getVariants().stream()
                .min(Comparator.comparing(v -> (v.getDiscountPrice() != null && v.getDiscountPrice() > 0) 
                    ? v.getDiscountPrice() : v.getPrice()))
                .orElse(perfume.getVariants().get(0));

        // 3. ANA SAHƏLƏRİ̇ DOLDURURUQ (Frontend-in birbaşa istifadəsi üçün)
        dto.setPrice(cheapestVariant.getPrice()); // Orijinal qiymət (məs: 210)
        dto.setDiscountPrice(cheapestVariant.getDiscountPrice()); // Endirimli qiymət (məs: 195)
        
        // minPrice - hər zaman görünən ən aşağı qiymət (endirim varsa endirimli olan)
        Double effectiveMinPrice = (cheapestVariant.getDiscountPrice() != null && cheapestVariant.getDiscountPrice() > 0) 
                ? cheapestVariant.getDiscountPrice() : cheapestVariant.getPrice();
        dto.setMinPrice(effectiveMinPrice);

        // 4. Variantları ML-ə görə sıralayıb DTO siyahısına çeviririk
        List<PerfumeVariant> sortedVariants = perfume.getVariants().stream()
                .sorted(Comparator.comparing(PerfumeVariant::getMl)).toList();

        List<PerfumeVariantDTO> variantDtos = sortedVariants.stream().map(v -> {
            PerfumeVariantDTO vDto = new PerfumeVariantDTO();
            vDto.setId(v.getId());
            vDto.setMl(v.getMl());
            vDto.setPrice(v.getPrice());
            vDto.setDiscountPrice(v.getDiscountPrice());
            vDto.setStock(v.getStock());
            if (v.getStock() > 0 && v.getStock() <= 3) {
                vDto.setLowStock(true);
                vDto.setStockMessage("Only " + v.getStock() + " left!");
            }
            return vDto;
        }).toList();

        dto.setVariants(variantDtos);

        // 5. Default ML təyini (Ən ucuz variantın ML-ni default edirik)
        dto.setDefaultMl(cheapestVariant.getMl());
    }

    return dto;
}
}