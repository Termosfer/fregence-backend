package com.fregence.fregence.service;

import java.io.IOException;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fregence.fregence.entity.Perfume;
import com.fregence.fregence.entity.Gender;
import com.fregence.fregence.config.RedisConfig;
import com.fregence.fregence.dto.PagedResponse;
import com.fregence.fregence.dto.PerfumeDTO; // DTO importu mütləqdir
import com.fregence.fregence.repository.CartItemRepository;
import com.fregence.fregence.repository.PerfumeRepository;
import com.fregence.fregence.repository.WishlistRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;

@Service
public class PerfumeService {

    private final RedisConfig redisConfig;

	private final FileService fileService;

	private final PerfumeRepository repository;
	@Autowired private WishlistRepository wishlistRepository;
	@Autowired private CartItemRepository cartItemRepository;
	public PerfumeService(PerfumeRepository repository, FileService fileService, RedisConfig redisConfig) {
		this.repository = repository;
		this.fileService = fileService;
		this.redisConfig = redisConfig;
	}

	// 1. Yeni ətir əlavə etmək (DTO qaytarır)
	// 2. Yeni ətir əlavə olunanda köhnə keş (siyahı) artıq yalandır, onu sil!
	@CacheEvict(value = "perfumes", allEntries = true)
	@Transactional
	public PerfumeDTO savePerfume(Perfume perfume) {
		Perfume savedPerfume = repository.save(perfume);
		return convertToDto(savedPerfume);
	}

	// 2. Bütün ətirləri və ya axtarışa görə gətirmək (Page<DTO> qaytarır)
	// 1. Siyahını gətirəndə Redis-ə bax, yoxdursa bazadan götür və Redis-ə qoy
	@Cacheable(value = "perfumes", key = "(#query ?: 'default') + '-' + #pageable.pageNumber + '-' + #pageable.pageSize + '-' + #pageable.sort.toString()")
	public PagedResponse<PerfumeDTO> getAllPerfumes(String query, Pageable pageable) {
	    Page<Perfume> perfumes;
	    if (query != null && !query.isEmpty()) {
	        perfumes = repository.findByNameContainingIgnoreCaseOrBrandContainingIgnoreCase(query, query, pageable);
	    } else {
	        perfumes = repository.findAll(pageable);
	    }

	    // Page-i PagedResponse-a çeviririk
	    Page<PerfumeDTO> dtoPage = perfumes.map(this::convertToDto);
	    
	    return new PagedResponse<>(
	        dtoPage.getContent(),
	        dtoPage.getNumber(),
	        dtoPage.getSize(),
	        dtoPage.getTotalElements(),
	        dtoPage.getTotalPages(),
	        dtoPage.isLast()
	    );
	}

	// Brendləri gətirir və Redis-də keşləyir
	@Cacheable(value = "perfumes", key = "'all-brands'")
	public List<String> getAllBrands() {
	    return repository.findUniqueBrands();
	}
	
	// 3. ID-yə görə tək bir ətir (DTO qaytarır)
	public PerfumeDTO getPerfumeById(Long id) {
		Perfume perfume = repository.findById(id)
				.orElseThrow(() -> new RuntimeException("Perfume not found with id: " + id));
		return convertToDto(perfume);
	}

	// 4. Ətiri redaktə etmək (DTO qaytarır)
	@Transactional
	public PerfumeDTO updatePerfume(Long id, Perfume updatedPerfume) {
		Perfume existingPerfume = repository.findById(id).orElseThrow(() -> new RuntimeException("Perfume not found"));

		existingPerfume.setName(updatedPerfume.getName());
		existingPerfume.setBrand(updatedPerfume.getBrand());
		existingPerfume.setPrice(updatedPerfume.getPrice());
		existingPerfume.setDiscountPrice(updatedPerfume.getDiscountPrice());
		existingPerfume.setDescription(updatedPerfume.getDescription());
		existingPerfume.setImageUrl(updatedPerfume.getImageUrl());
		existingPerfume.setMl(updatedPerfume.getMl());
		existingPerfume.setGender(updatedPerfume.getGender());
		existingPerfume.setStock(updatedPerfume.getStock());
		existingPerfume.setIsNew(updatedPerfume.getIsNew());
		// Recommendation sahəsini buradan sildik

		return convertToDto(repository.save(existingPerfume));
	}

	// 5. Ətiri silmək
	// 3. Ətir silinəndə də keşi təmizlə
	@CacheEvict(value = "perfumes", allEntries = true)
	@Transactional
	public void deletePerfume(Long id) {
	    Perfume perfume = repository.findById(id)
	            .orElseThrow(() -> new RuntimeException("Perfume not found"));

	    // Silməzdən əvvəl başqa cədvəllərdəki izləri silirik:
	    wishlistRepository.deleteByPerfume(perfume);
	    cartItemRepository.deleteByPerfume(perfume);

	    // Cloudinary-dən şəkli silirik:
	    if (perfume.getImagePublicId() != null) {
	        fileService.deleteImage(perfume.getImagePublicId());
	    }

	    // İndi artıq əsas cədvəldən silə bilərik:
	    repository.delete(perfume);
	}

	
	// 6. Filtr Metodu (PagedResponse qaytarır və Redis-də keşlənir)
	@Cacheable(value = "perfumes", 
	           key = "'filter-' + (#brand ?: 'all') + '-' + (#gender ?: 'all') + '-' + (#minPrice ?: '0') + '-' + (#maxPrice ?: 'max') + '-' + #pageable.pageNumber + '-' + #pageable.pageSize + '-' + #pageable.sort.toString()")
	public PagedResponse<PerfumeDTO> filterPerfumes(String brand, Gender gender, Double minPrice, Double maxPrice, Pageable pageable) {
	    
	    // 1. Bazadan Page<Perfume> (Entity) gətiririk
	    Page<Perfume> perfumes = repository.filterPerfumes(brand, gender, minPrice, maxPrice, pageable);
	    
	    // 2. Entity-ləri DTO-ya çeviririk
	    Page<PerfumeDTO> dtoPage = perfumes.map(this::convertToDto);
	    
	    // 3. PagedResponse obyektinə büküb qaytarırıq
	    return new PagedResponse<>(
	        dtoPage.getContent(),
	        dtoPage.getNumber(),
	        dtoPage.getSize(),
	        dtoPage.getTotalElements(),
	        dtoPage.getTotalPages(),
	        dtoPage.isLast()
	    );
	}

	public java.util.List<PerfumeDTO> getRecommendedPerfumes() {
		return repository.findByIsRecommendedTrue().stream().map(this::convertToDto)
				.collect(java.util.stream.Collectors.toList());
	}

	public java.util.List<PerfumeDTO> getRelatedPerfumes(Long id) {
		// Əvvəlcə baxılan ətiri tapırıq ki, brendini bilək
		Perfume perfume = repository.findById(id).orElseThrow(() -> new RuntimeException("Perfume not found"));

		return repository.findTop4ByBrandAndIdNot(perfume.getBrand(), id).stream().map(this::convertToDto)
				.collect(java.util.stream.Collectors.toList());
	}

	// 7. Entity-ni DTO-ya çevirən köməkçi metod
	private PerfumeDTO convertToDto(Perfume perfume) {
		PerfumeDTO dto = new PerfumeDTO();
		dto.setId(perfume.getId());
		dto.setBrand(perfume.getBrand());
		dto.setName(perfume.getName());
		dto.setPrice(perfume.getPrice());
		dto.setDiscountPrice(perfume.getDiscountPrice());
		dto.setImageUrl(perfume.getImageUrl());
		dto.setDescription(perfume.getDescription());
		dto.setMl(perfume.getMl());

		if (perfume.getGender() != null) {
			dto.setGender(perfume.getGender().name());
		}

		dto.setIsNew(perfume.getIsNew());
		dto.setIsRecommended(perfume.getIsRecommended());
		return dto;
	}
}