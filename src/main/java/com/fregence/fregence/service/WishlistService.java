package com.fregence.fregence.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fregence.fregence.dto.PerfumeDTO;
import com.fregence.fregence.entity.Perfume;
import com.fregence.fregence.entity.User;
import com.fregence.fregence.entity.Wishlist;
import com.fregence.fregence.repository.PerfumeRepository;
import com.fregence.fregence.repository.WishlistRepository;

@Service
public class WishlistService {

    @Autowired private WishlistRepository wishlistRepository;
    @Autowired private PerfumeRepository perfumeRepository;
    @Autowired private UserService userService;

    // 1. Siyahını gətirmək (Xəta verən hissə)
    public List<PerfumeDTO> getMyWishlist() {
        User user = userService.getCurrentUser();
        List<Wishlist> wishes = wishlistRepository.findByUser(user);

        return wishes.stream()
                .map(item -> convertToDto(item.getPerfume())) // Buraya diqqət: {} və return yazmağa ehtiyac yoxdur
                .toList();
    }

    // 2. Əlavə etmək
    public void addToWishlist(Long perfumeId) {
        User user = userService.getCurrentUser();
        Perfume perfume = perfumeRepository.findById(perfumeId)
                .orElseThrow(() -> new RuntimeException("Ətir tapılmadı"));

        if (!wishlistRepository.existsByUserAndPerfume(user, perfume)) {
            Wishlist wishlist = new Wishlist();
            wishlist.setUser(user);
            wishlist.setPerfume(perfume);
            wishlistRepository.save(wishlist);
        }
    }

    // 3. Silmək
    @Transactional
    public void removeFromWishlist(Long perfumeId) {
        User user = userService.getCurrentUser();
        Perfume perfume = perfumeRepository.findById(perfumeId)
                .orElseThrow(() -> new RuntimeException("Ətir tapılmadı"));
        wishlistRepository.deleteByUserAndPerfume(user, perfume);
    }

    // 4. VACİB: Bu metod mütləq Perfume qəbul etməli və PerfumeDTO qaytarmalıdır!
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
        return dto; // Mütləq dto-nu return etməlisən!
    }
}