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
    
    @Autowired private OrderItemRepository orderItemRepository;
    @Autowired private WishlistRepository wishlistRepository;
    @Autowired private CartItemRepository cartItemRepository;

    public PerfumeService(PerfumeRepository repository, FileService fileService) {
        this.repository = repository;
        this.fileService = fileService;
    }

    @CacheEvict(value = {"perfumes", "perfume-details", "perfume-related"}, allEntries = true)
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

        return new PagedResponse<>(
            perfumes.getContent().stream().map(this::convertToDto).toList(),
            perfumes.getNumber(),
            perfumes.getSize(),
            perfumes.getTotalElements(),
            perfumes.getTotalPages(),
            perfumes.isLast()
        );
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

    @CacheEvict(value = {"perfumes", "perfume-details", "perfume-related"}, allEntries = true)
    @Transactional
    public PerfumeDTO updatePerfume(Long id, Perfume updatedPerfume) {
        Perfume existingPerfume = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Perfume not found"));

        existingPerfume.setName(updatedPerfume.getName());
        existingPerfume.setBrand(updatedPerfume.getBrand());
        existingPerfume.setDescription(updatedPerfume.getDescription());
        if (updatedPerfume.getImageUrl() != null) {
            existingPerfume.setImageUrl(updatedPerfume.getImageUrl());
        }
        existingPerfume.setGender(updatedPerfume.getGender());
        existingPerfume.setIsNew(updatedPerfume.getIsNew());
        existingPerfume.setIsRecommended(updatedPerfume.getIsRecommended());

        // Variantları yeniləyirik
        if (updatedPerfume.getVariants() != null) {
            existingPerfume.getVariants().clear();
            updatedPerfume.getVariants().forEach(v -> {
                v.setPerfume(existingPerfume);
                existingPerfume.getVariants().add(v);
            });
        }

        return convertToDto(repository.save(existingPerfume));
    }

    @CacheEvict(value = {"perfumes", "perfume-details", "perfume-related"}, allEntries = true)
    @Transactional
    public void deletePerfume(Long id) {
        Perfume perfume = repository.findById(id).orElseThrow(() -> new RuntimeException("Perfume not found"));

        // Referansları təmizləyirik
        List<OrderItem> orderItems = orderItemRepository.findByPerfumeId(id);
        if (!orderItems.isEmpty()) {
            orderItems.forEach(item -> item.setPerfume(null));
            orderItemRepository.saveAll(orderItems);
        }

        wishlistRepository.deleteByPerfume(perfume);
        cartItemRepository.deleteByPerfume(perfume);

        if (perfume.getImagePublicId() != null) {
            fileService.deleteImage(perfume.getImagePublicId());
        }

        repository.delete(perfume);
    }

    @Cacheable(value = "perfumes", key = "'filter-' + (#brand ?: 'all') + '-' + (#gender ?: 'all') + '-' + (#ml ?: 'all') + '-' + (#minPrice ?: '0') + '-' + (#maxPrice ?: 'max') + '-' + #pageable.pageNumber + '-' + #pageable.pageSize + '-' + #pageable.sort.toString()")
    public PagedResponse<PerfumeDTO> filterPerfumes(String brand, Gender gender, Integer ml, Double minPrice, Double maxPrice, Pageable pageable) {
        Page<Perfume> perfumes = repository.filterPerfumes(brand, gender, minPrice, maxPrice, ml, pageable);
        
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
    private PerfumeDTO convertToDto(Perfume perfume) {
    PerfumeDTO dto = new PerfumeDTO();
    // ... təməl set-lər (id, brand, name, description, imageUrl və s.) ...

    if (perfume.getVariants() != null && !perfume.getVariants().isEmpty()) {
        // 1. Variantları ML-ə görə sıralayırıq (kiçikdən böyüyə)
        List<PerfumeVariant> sortedVariants = perfume.getVariants().stream()
                .sorted(Comparator.comparing(PerfumeVariant::getMl))
                .toList();

        // 2. Variant DTO-larını yaradırıq (Bayaqkı stok mesajları ilə birlikdə)
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

        // 3. VACİB: DEFAULT ML SEÇİ̇Mİ̇ (Priority: 100 > 75 > Largest)
        List<Integer> availableMls = sortedVariants.stream()
                .map(PerfumeVariant::getMl)
                .toList();

        Integer finalDefaultMl;
        if (availableMls.contains(100)) {
            finalDefaultMl = 100;
        } else if (availableMls.contains(75)) {
            finalDefaultMl = 75;
        } else {
            // Əgər 100 və ya 75 yoxdursa, əldə olan ən böyük ölçünü götür
            finalDefaultMl = availableMls.get(availableMls.size() - 1);
        }
        
        dto.setDefaultMl(finalDefaultMl);

        // 4. Qiymət hesablanması (Min Price)
        Double calculatedMinPrice = variantDtos.stream()
            .map(v -> (v.getDiscountPrice() != null && v.getDiscountPrice() > 0) ? v.getDiscountPrice() : v.getPrice())
            .min(Comparator.naturalOrder())
            .orElse(0.0);
        
        dto.setMinPrice(calculatedMinPrice);
        dto.setPrice(calculatedMinPrice);
    }
    
    return dto;
}
}