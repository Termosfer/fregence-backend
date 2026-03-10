package com.fregence.fregence.controller;

import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.fregence.fregence.dto.SubscriberDTO;
import com.fregence.fregence.entity.Subscriber;
import com.fregence.fregence.service.SubscriberService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/subscribers")
public class SubscriberController {

    private final SubscriberService service;

    public SubscriberController(SubscriberService service) {
        this.service = service;
    }

    // Abunə olmaq
    @PostMapping
    public ResponseEntity<SubscriberDTO> subscribe(@Valid @RequestBody Subscriber subscriber) {
        return ResponseEntity.ok(service.subscribe(subscriber.getEmail()));
    }

    // Admin üçün siyahı
    @GetMapping
    public ResponseEntity<Page<SubscriberDTO>> getSubscribers(
            @RequestParam(required = false) String query,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        return ResponseEntity.ok(service.getSubscribers(query, page, size));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.ok("Subscriber deleted successfully");
    }
}