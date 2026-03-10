package com.fregence.fregence.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.fregence.fregence.dto.PerfumeDTO; // <--- Bu import mütləq olmalıdır
import com.fregence.fregence.entity.Perfume;
import com.fregence.fregence.entity.Gender;
import com.fregence.fregence.service.FileService;
import com.fregence.fregence.service.PerfumeService;

@RestController
@RequestMapping("/api/perfumes")
public class PerfumeController {

    private final PerfumeService service;
    @Autowired
    private FileService fileService;
    public PerfumeController(PerfumeService service) {
        this.service = service;
    }

    // 1. Hamı üçün: Siyahı (Page<PerfumeDTO> qaytarır)
    @GetMapping
    public ResponseEntity<Page<PerfumeDTO>> getAll(
            @RequestParam(required = false) String query,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size,
            @RequestParam(defaultValue = "id") String sortBy) {
        
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortBy).descending());
        return ResponseEntity.ok(service.getAllPerfumes(query, pageable));
    }

    // 2. Hamı üçün: Detal (PerfumeDTO qaytarır)
    @GetMapping("/{id}")
    public ResponseEntity<PerfumeDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(service.getPerfumeById(id));
    }

    // 3. Hamı üçün: Filtrləmə (Page<PerfumeDTO> qaytarır)
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

    // 4. ADMIN: Yaratmaq (RequestBody Entity qəbul edir, amma DTO qaytarır)
    @PostMapping
    public ResponseEntity<PerfumeDTO> create(@RequestBody Perfume perfume) {
        return ResponseEntity.ok(service.savePerfume(perfume));
    }

    // 5. ADMIN: Update (PerfumeDTO qaytarır)
    @PutMapping("/{id}")
    public ResponseEntity<PerfumeDTO> update(@PathVariable Long id, @RequestBody Perfume perfume) {
        return ResponseEntity.ok(service.updatePerfume(id, perfume));
    }

    // 6. ADMIN: Silmək (ResponseEntity<String> dəyişmir)
    @DeleteMapping("/{id}")
    public ResponseEntity<String> delete(@PathVariable Long id) {
        service.deletePerfume(id);
        return ResponseEntity.ok("Perfume deleted successfully with id: " + id);
    }
    
 // 1. Tövsiyə olunanlar (Ana səhifə üçün)
    @GetMapping("/recommendations")
    public ResponseEntity<java.util.List<PerfumeDTO>> getRecommendations() {
        return ResponseEntity.ok(service.getRecommendedPerfumes());
    }

    // 2. Oxşar ətirlər (Məhsulun öz səhifəsi üçün)
    @GetMapping("/{id}/related")
    public ResponseEntity<java.util.List<PerfumeDTO>> getRelated(@PathVariable Long id) {
        return ResponseEntity.ok(service.getRelatedPerfumes(id));
    }
    
    @PostMapping(consumes = {"multipart/form-data"})
    public ResponseEntity<PerfumeDTO> create(
            @RequestPart("perfume") String perfumeJson, // JSON String kimi gəlir
            @RequestPart("image") org.springframework.web.multipart.MultipartFile image) throws Exception {
        
        // 1. JSON-u obyekti çevir
        com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
        Perfume perfume = mapper.readValue(perfumeJson, Perfume.class);

        // 2. Şəkli yadda saxla və URL-i təyin et
        if (image != null && !image.isEmpty()) {
            String fileName = fileService.saveImage(image);
            perfume.setImageUrl("/uploads/perfumes/" + fileName);
        }

        return ResponseEntity.ok(service.savePerfume(perfume));
    }
    
}