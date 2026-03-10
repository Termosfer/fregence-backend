package com.fregence.fregence.controller;

import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.fregence.fregence.dto.ContactMessageDTO;
import com.fregence.fregence.entity.ContactMessage;
import com.fregence.fregence.service.ContactMessageService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/contact")
public class ContactMessageController {

    private final ContactMessageService service;

    public ContactMessageController(ContactMessageService service) {
        this.service = service;
    }

    // Mesaj göndər (User/Admin)
    @PostMapping
    public ResponseEntity<ContactMessageDTO> sendMessage(@Valid @RequestBody ContactMessage message) {
        return ResponseEntity.ok(service.saveMessage(message));
    }

    // Mesajları siyahıla (Yalnız Admin)
    @GetMapping
    public ResponseEntity<Page<ContactMessageDTO>> getMessages(
            @RequestParam(required = false) String query,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        return ResponseEntity.ok(service.getMessages(query, page, size));
    }

    // Mesajı sil (Yalnız Admin)
    @DeleteMapping("/{id}")
    public ResponseEntity<String> delete(@PathVariable Long id) {
        service.deleteMessage(id);
        return ResponseEntity.ok("Message deleted successfully");
    }
}