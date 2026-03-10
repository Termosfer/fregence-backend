package com.fregence.fregence.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.fregence.fregence.dto.ContactMessageDTO;
import com.fregence.fregence.entity.ContactMessage;
import com.fregence.fregence.repository.ContactMessageRepository;

@Service
public class ContactMessageService {

    private final ContactMessageRepository repository;

    public ContactMessageService(ContactMessageRepository repository) {
        this.repository = repository;
    }

    // 1. Mesajı yadda saxla (DTO qaytarır)
    public ContactMessageDTO saveMessage(ContactMessage message) {
        ContactMessage saved = repository.save(message);
        return convertToDto(saved);
    }

    // 2. Səhifələmə və Axtarış (Page<DTO> qaytarır)
    public Page<ContactMessageDTO> getMessages(String query, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<ContactMessage> messages;

        if (query == null || query.isEmpty()) {
            messages = repository.findAll(pageable);
        } else {
            messages = repository.findByEmailContainingIgnoreCaseOrNameContainingIgnoreCase(query, query, pageable);
        }

        return messages.map(this::convertToDto);
    }

    // 3. Mesajı sil (Eyni qalır)
    public void deleteMessage(Long id) {
        if (!repository.existsById(id)) {
            throw new RuntimeException("Message not found with id: " + id);
        }
        repository.deleteById(id);
    }

    // Köməkçi metod: Entity -> DTO
    private ContactMessageDTO convertToDto(ContactMessage message) {
        return new ContactMessageDTO(
            message.getId(),
            message.getName(),
            message.getEmail(),
            message.getMessage(),
            message.getCreatedAt()
        );
    }
}