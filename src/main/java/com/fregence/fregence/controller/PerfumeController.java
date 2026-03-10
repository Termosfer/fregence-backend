package com.fregence.fregence.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType; // Düzgün import
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.fregence.fregence.dto.PerfumeDTO;
import com.fregence.fregence.entity.Perfume;
import com.fregence.fregence.entity.Gender;
import com.fregence.fregence.service.FileService;
import com.fregence.fregence.service.PerfumeService;

import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/perfumes")
public class PerfumeController {

    private final PerfumeService service;
    
    @Autowired
    private FileService fileService;

    public PerfumeController(PerfumeService service) {
        this.service = service;
    }

    // 1. Siyahı və Axtarış
    @GetMapping
    public ResponseEntity<Page<PerfumeDTO>> getAll(
            @RequestParam(required = false) String query,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size,
            @RequestParam(defaultValue = "id") String sortBy) {
        
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortBy).descending());
        return ResponseEntity.ok(service.getAllPerfumes(query, pageable));
    }

    // 2. Detal
    @GetMapping("/{id}")
    public ResponseEntity<PerfumeDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(service.getPerfumeById(id));
    }

    // 3. Filtrləmə
    @GetMapping("/filter")
    public ResponseEntity<Page<PerfumeDTO>> filter(
            @RequestParam(required = false) String brand,
            @RequestParam(required = false) Gender gender,
            @RequestParam(required = false) Double minPrice,
            @RequestParam(required = false) Double maxPrice,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size) {
        
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(service.filterPerfumes(brand, gender, minPrice, maxPrice, pageable));
    }

    // 4. ADMIN: Yeni ətir yaratmaq (Şəkil yükləmə ilə birlikdə)
    @PostMapping(value = "", consumes = { MediaType.MULTIPART_FORM_DATA_VALUE })
    public ResponseEntity<PerfumeDTO> create(
            @RequestPart("perfume") @Valid Perfume perfume,
            @RequestPart("image") MultipartFile image) throws Exception {
        
        if (image != null && !image.isEmpty()) {
            // Cloudinary-dən gələn linki birbaşa mənimsədirik (təkrar artırmalar yoxdur)
            String fullCloudinaryUrl = fileService.saveImage(image);
            perfume.setImageUrl(fullCloudinaryUrl);
        }

        return ResponseEntity.ok(service.savePerfume(perfume));
    }

    // 5. ADMIN: Yeniləmək
    @PutMapping("/{id}")
    public ResponseEntity<PerfumeDTO> update(@PathVariable Long id, @RequestBody Perfume perfume) {
        return ResponseEntity.ok(service.updatePerfume(id, perfume));
    }

    // 6. ADMIN: Silmək
    @DeleteMapping("/{id}")
    public ResponseEntity<String> delete(@PathVariable Long id) {
        service.deletePerfume(id);
        return ResponseEntity.ok("Perfume deleted successfully with id: " + id);
    }
    
    // 7. Tövsiyələr
    @GetMapping("/recommendations")
    public ResponseEntity<List<PerfumeDTO>> getRecommendations() {
        return ResponseEntity.ok(service.getRecommendedPerfumes());
    }

    // 8. Oxşar ətirlər
    @GetMapping("/{id}/related")
    public ResponseEntity<List<PerfumeDTO>> getRelated(@PathVariable Long id) {
        return ResponseEntity.ok(service.getRelatedPerfumes(id));
    }
}