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
import com.fregence.fregence.service.WishlistService;

@RestController
@RequestMapping("/api/wishlist")
public class WishlistController {
    @Autowired 
    private WishlistService wishlistService;

    @GetMapping
    public ResponseEntity<List<PerfumeDTO>> getWishlist() {
        return ResponseEntity.ok(wishlistService.getMyWishlist());
    }

    @PostMapping("/add/{perfumeId}")
    public ResponseEntity<String> add(@PathVariable Long perfumeId) {
        wishlistService.addToWishlist(perfumeId);
        return ResponseEntity.ok("Məhsul istək siyahısına əlavə edildi.");
    }

    @DeleteMapping("/remove/{perfumeId}")
    public ResponseEntity<String> remove(@PathVariable Long perfumeId) {
        wishlistService.removeFromWishlist(perfumeId);
        return ResponseEntity.ok("Məhsul istək siyahısından silindi.");
    }
}