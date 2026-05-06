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
        dto.setId(perfume.getId());
        dto.setBrand(perfume.getBrand());
        dto.setName(perfume.getName());
        dto.setDescription(perfume.getDescription());
        dto.setImageUrl(perfume.getImageUrl());
        dto.setIsNew(perfume.getIsNew());
        dto.setIsRecommended(perfume.getIsRecommended());

        if (perfume.getGender() != null) {
            dto.setGender(perfume.getGender().name());
        }

        if (perfume.getVariants() != null && !perfume.getVariants().isEmpty()) {
            List<PerfumeVariantDTO> variantDtos = perfume.getVariants().stream().map(v -> {
                PerfumeVariantDTO vDto = new PerfumeVariantDTO();
                vDto.setId(v.getId());
                vDto.setMl(v.getMl());
                vDto.setPrice(v.getPrice());
                vDto.setDiscountPrice(v.getDiscountPrice());
                vDto.setStock(v.getStock());

                if (v.getStock() > 0 && v.getStock() <= 3) {
                    vDto.setLowStock(true);
                    vDto.setStockMessage("Only " + v.getStock() + " left!");
                } else if (v.getStock() == 0) {
                    vDto.setLowStock(false);
                    vDto.setStockMessage("Out of Stock");
                }
                return vDto;
            }).toList();

            dto.setVariants(variantDtos);

            Double minPrice = variantDtos.stream()
                .map(v -> (v.getDiscountPrice() != null && v.getDiscountPrice() > 0) ? v.getDiscountPrice() : v.getPrice())
                .min(Comparator.naturalOrder())
                .orElse(0.0);
            
            dto.setPrice(minPrice);
        }

        return dto;
    }
}