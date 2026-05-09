package com.fregence.fregence.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fregence.fregence.dto.PerfumeDTO;
import com.fregence.fregence.dto.WishlistItemDTO;
import com.fregence.fregence.service.WishlistService;

@RestController
@RequestMapping("/api/wishlist")
public class WishlistController {
    
    @Autowired private WishlistService wishlistService;

    @GetMapping
    public ResponseEntity<List<WishlistItemDTO>> getWishlist() {
        return ResponseEntity.ok(wishlistService.getMyWishlist());
    }

    @GetMapping("/count")
    public ResponseEntity<Long> getCount() {
        return ResponseEntity.ok(wishlistService.getWishlistCount());
    }
    @PostMapping("/add/{variantId}") // Artıq variant ID-si ilə əlavə edirik
    public ResponseEntity<String> add(@PathVariable Long variantId) {
        wishlistService.addToWishlist(variantId);
        return ResponseEntity.ok("Məhsulun bu ölçüsü istək siyahısına əlavə edildi.");
    }

    @DeleteMapping("/remove/{variantId}")
    public ResponseEntity<String> remove(@PathVariable Long variantId) {
        wishlistService.removeFromWishlist(variantId);
        return ResponseEntity.ok("Məhsul istək siyahısından silindi.");
    }
}