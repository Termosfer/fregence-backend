package com.fregence.fregence.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fregence.fregence.dto.CartDTO;
import com.fregence.fregence.service.CartService;

@RestController
@RequestMapping("/api/cart")
public class CartController {
    
    @Autowired 
    private CartService cartService;

    @GetMapping
    public ResponseEntity<CartDTO> getCart() {
        return ResponseEntity.ok(cartService.getMyCart());
    }

    // YENİ: Səbətdəki məhsul növlərinin sayını qaytarır
    @GetMapping("/count")
    public ResponseEntity<Integer> getCartCount() {
        return ResponseEntity.ok(cartService.getCartCount());
    }

    @PostMapping("/add")
    public ResponseEntity<String> add(@RequestParam Long variantId, @RequestParam int quantity) {
        cartService.addToCart(variantId, quantity);
        return ResponseEntity.ok("Məhsul səbətə əlavə edildi.");
    }

    @DeleteMapping("/remove/{itemId}")
    public ResponseEntity<String> remove(@PathVariable Long itemId) {
        cartService.removeItem(itemId);
        return ResponseEntity.ok("Məhsul səbətdən silindi.");
    }
}