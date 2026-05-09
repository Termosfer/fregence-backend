package com.fregence.fregence.service;

import com.fregence.fregence.dto.WishlistItemDTO;
import com.fregence.fregence.entity.PerfumeVariant; // MÜTLƏQ İMPORT ET
import com.fregence.fregence.entity.User;
import com.fregence.fregence.entity.Wishlist;
import com.fregence.fregence.repository.WishlistRepository;
import com.fregence.fregence.repository.PerfumeVariantRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class WishlistService {

    @Autowired private WishlistRepository wishlistRepository;
    @Autowired private PerfumeVariantRepository variantRepository;
    @Autowired private UserService userService;

    public List<WishlistItemDTO> getMyWishlist() {
        User user = userService.getCurrentUser();
        List<Wishlist> wishes = wishlistRepository.findByUser(user);

        return wishes.stream().map(wish -> {
            WishlistItemDTO dto = new WishlistItemDTO();
            dto.setWishlistId(wish.getId());
            
            PerfumeVariant v = wish.getVariant();
            dto.setVariantId(v.getId());
            dto.setPerfumeId(v.getPerfume().getId());
            dto.setBrand(v.getPerfume().getBrand());
            dto.setName(v.getPerfume().getName());
            dto.setImageUrl(v.getPerfume().getImageUrl());
            dto.setMl(v.getMl());
            dto.setPrice(v.getPrice());
            dto.setDiscountPrice(v.getDiscountPrice());
            
            return dto;
        }).toList();
    }

    public void addToWishlist(Long variantId) {
        User user = userService.getCurrentUser();
        PerfumeVariant variant = variantRepository.findById(variantId)
                .orElseThrow(() -> new RuntimeException("Variant tapılmadı"));

        if (!wishlistRepository.existsByUserAndVariant(user, variant)) {
            Wishlist wishlist = new Wishlist();
            wishlist.setUser(user);
            wishlist.setVariant(variant);
            wishlistRepository.save(wishlist);
        }
    }

    @Transactional
    public void removeFromWishlist(Long variantId) {
        User user = userService.getCurrentUser();
        PerfumeVariant variant = variantRepository.findById(variantId)
                .orElseThrow(() -> new RuntimeException("Variant tapılmadı"));
        wishlistRepository.deleteByUserAndVariant(user, variant);
    }
    
    public long getWishlistCount() {
        User user = userService.getCurrentUser();
        return wishlistRepository.countByUser(user);
    }
}