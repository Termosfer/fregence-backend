package com.fregence.fregence.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fregence.fregence.entity.Perfume;
import com.fregence.fregence.entity.Gender;
import com.fregence.fregence.dto.PerfumeDTO; // DTO importu mütləqdir
import com.fregence.fregence.repository.PerfumeRepository;

@Service
public class PerfumeService {

    private final PerfumeRepository repository;

    public PerfumeService(PerfumeRepository repository) {
        this.repository = repository;
    }

    // 1. Yeni ətir əlavə etmək (DTO qaytarır)
    @Transactional
    public PerfumeDTO savePerfume(Perfume perfume) {
        Perfume savedPerfume = repository.save(perfume);
        return convertToDto(savedPerfume);
    }

    // 2. Bütün ətirləri və ya axtarışa görə gətirmək (Page<DTO> qaytarır)
    public Page<PerfumeDTO> getAllPerfumes(String query, Pageable pageable) {
        Page<Perfume> perfumes;
        if (query != null && !query.isEmpty()) {
            perfumes = repository.findByNameContainingIgnoreCaseOrBrandContainingIgnoreCase(query, query, pageable);
        } else {
            perfumes = repository.findAll(pageable);
        }
        return perfumes.map(this::convertToDto);
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
        Perfume existingPerfume = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Perfume not found"));
        
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
    @Transactional
    public void deletePerfume(Long id) {
        if (!repository.existsById(id)) {
            throw new RuntimeException("Perfume not found with id: " + id);
        }
        repository.deleteById(id);
    }

    // 6. Filtr Metodu (Page<DTO> qaytarır)
    public Page<PerfumeDTO> filterPerfumes(String brand, Gender gender, Double minPrice, Double maxPrice, Pageable pageable) {
        Page<Perfume> perfumes = repository.filterPerfumes(brand, gender, minPrice, maxPrice, pageable);
        return perfumes.map(this::convertToDto);
    }

    public java.util.List<PerfumeDTO> getRecommendedPerfumes() {
        return repository.findByIsRecommendedTrue()
                .stream()
                .map(this::convertToDto)
                .collect(java.util.stream.Collectors.toList());
    }

    public java.util.List<PerfumeDTO> getRelatedPerfumes(Long id) {
        // Əvvəlcə baxılan ətiri tapırıq ki, brendini bilək
        Perfume perfume = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Perfume not found"));
        
        return repository.findTop4ByBrandAndIdNot(perfume.getBrand(), id)
                .stream()
                .map(this::convertToDto)
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