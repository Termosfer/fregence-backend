package com.fregence.fregence.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.fregence.fregence.dto.SubscriberDTO; // DTO import et
import com.fregence.fregence.entity.Subscriber;
import com.fregence.fregence.repository.SubscriberRepository;

@Service
public class SubscriberService {

    private final SubscriberRepository repository;

    public SubscriberService(SubscriberRepository repository) {
        this.repository = repository;
    }

    // 1. Abunə olmaq (DTO qaytarır)
    public SubscriberDTO subscribe(String email) {
        if (repository.existsByEmail(email)) {
            throw new RuntimeException("Email already subscribed");
        }

        Subscriber subscriber = new Subscriber();
        subscriber.setEmail(email);
        
        Subscriber saved = repository.save(subscriber);
        return convertToDto(saved); // DTO-ya çevirib qaytarırıq
    }

    // 2. Siyahı və Axtarış (Page<DTO> qaytarır)
    public Page<SubscriberDTO> getSubscribers(String query, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Subscriber> subscribers;

        if (query == null || query.isEmpty()) {
            subscribers = repository.findAll(pageable);
        } else {
            subscribers = repository.findByEmailContainingIgnoreCase(query, pageable);
        }

        return subscribers.map(this::convertToDto); // Page daxilindəki hər entity-ni DTO-ya çevirir
    }

    // 3. Silmək (Eyni qalır)
    public void delete(Long id) {
        if (!repository.existsById(id)) {
            throw new RuntimeException("Subscriber not found");
        }
        repository.deleteById(id);
    }

    // Köməkçi metod: Entity -> DTO
    private SubscriberDTO convertToDto(Subscriber subscriber) {
        return new SubscriberDTO(subscriber.getId(), subscriber.getEmail());
    }
}