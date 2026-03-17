package com.fregence.fregence.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fregence.fregence.dto.PagedResponse;
import com.fregence.fregence.dto.PerfumeDTO;
import com.fregence.fregence.entity.Perfume;
import com.fregence.fregence.entity.Gender;
import com.fregence.fregence.service.FileService;
import com.fregence.fregence.service.PerfumeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/perfumes")
public class PerfumeController {

    private final PerfumeService service;

    @Autowired
    private FileService fileService;

    public PerfumeController(PerfumeService service) {
        this.service = service;
    }

    // 1. Siyahı (PagedResponse ilə)
    @GetMapping
    public ResponseEntity<PagedResponse<PerfumeDTO>> getAll(
            @RequestParam(required = false) String query,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size) {
        
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());
        return ResponseEntity.ok(service.getAllPerfumes(query, pageable));
    }

    // 2. ADMIN: Yeni ətir yaratmaq (Şəkil + JSON)
    @PostMapping(value = "", consumes = { MediaType.MULTIPART_FORM_DATA_VALUE })
    public ResponseEntity<PerfumeDTO> create(
            @RequestPart("perfume") String perfumeJson,
            @RequestPart(value = "image", required = false) MultipartFile image) throws Exception {
        
        ObjectMapper objectMapper = new ObjectMapper();
        Perfume perfume = objectMapper.readValue(perfumeJson, Perfume.class);

        if (image != null && !image.isEmpty()) {
            Map<String, String> imageData = fileService.uploadImage(image);
            perfume.setImageUrl(imageData.get("url"));
            perfume.setImagePublicId(imageData.get("public_id"));
        }

        return ResponseEntity.ok(service.savePerfume(perfume));
    }

    // 3. Detal
    @GetMapping("/{id}")
    public ResponseEntity<PerfumeDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(service.getPerfumeById(id));
    }

    // 4. Filtrləmə (Bunu da PagedResponse edirik!)
    @GetMapping("/filter")
    public ResponseEntity<PagedResponse<PerfumeDTO>> filter(
            @RequestParam(required = false) String brand,
            @RequestParam(required = false) Gender gender,
            @RequestParam(required = false) Double minPrice,
            @RequestParam(required = false) Double maxPrice,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size) {
        
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(service.filterPerfumes(brand, gender, minPrice, maxPrice, pageable));
    }

    // 5. Update
    @PutMapping("/{id}")
    public ResponseEntity<PerfumeDTO> update(@PathVariable Long id, @RequestBody Perfume perfume) {
        return ResponseEntity.ok(service.updatePerfume(id, perfume));
    }

    // 6. Delete
    @DeleteMapping("/{id}")
    public ResponseEntity<String> delete(@PathVariable Long id) {
        service.deletePerfume(id);
        return ResponseEntity.ok("Perfume deleted: " + id);
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