package com.fregence.fregence.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.fregence.fregence.dto.CartDTO;
import com.fregence.fregence.entity.Cart;
import com.fregence.fregence.entity.CartItem;
import com.fregence.fregence.entity.Perfume;
import com.fregence.fregence.entity.PerfumeVariant;
import com.fregence.fregence.entity.User;
import com.fregence.fregence.repository.CartItemRepository;
import com.fregence.fregence.repository.CartRepository;
import com.fregence.fregence.repository.PerfumeVariantRepository;

@ExtendWith(MockitoExtension.class)
public class CartServiceTest {

    @Mock private CartRepository cartRepository;
    @Mock private CartItemRepository itemRepository;
    @Mock private PerfumeVariantRepository variantRepository; // YENİ
    @Mock private UserService userService;

    @InjectMocks
    private CartService cartService;

    private User mockUser;
    private Cart mockCart;
    private Perfume mockPerfume;
    private PerfumeVariant mockVariant;

    @BeforeEach
    void setUp() {
        mockUser = new User();
        mockUser.setEmail("test@mail.com");

        mockCart = new Cart();
        mockCart.setId(10L);
        mockCart.setUser(mockUser);
        mockCart.setItems(new ArrayList<>());

        mockPerfume = new Perfume();
        mockPerfume.setId(1L);
        mockPerfume.setName("Sauvage");
        mockPerfume.setBrand("Dior");

        // YENİ: Variant yaradırıq
        mockVariant = new PerfumeVariant();
        mockVariant.setId(1L);
        mockVariant.setMl(100);
        mockVariant.setPrice(100.0);
        mockVariant.setDiscountPrice(80.0);
        mockVariant.setPerfume(mockPerfume);
    }

    @Test
    void addToCart_YeniMehsulVariantElaveEdilmeli() {
        // GIVEN
        when(userService.getCurrentUser()).thenReturn(mockUser);
        when(cartRepository.findByUser(mockUser)).thenReturn(Optional.of(mockCart));
        when(variantRepository.findById(1L)).thenReturn(Optional.of(mockVariant));

        // WHEN
        cartService.addToCart(1L, 2); // Artıq variantId göndəririk

        // THEN
        assertEquals(1, mockCart.getItems().size());
        assertEquals(1L, mockCart.getItems().get(0).getPerfumeVariant().getId());
        verify(cartRepository, times(1)).save(mockCart);
    }

    @Test
    void getMyCart_HesablamaMentiqiVariantlaDuzgunIshlemeli() {
        // GIVEN
        CartItem item = new CartItem();
        item.setPerfumeVariant(mockVariant); // Variantı set edirik
        item.setQuantity(2);
        item.setCart(mockCart);
        mockCart.getItems().add(item);

        when(userService.getCurrentUser()).thenReturn(mockUser);
        when(cartRepository.findByUser(mockUser)).thenReturn(Optional.of(mockCart));

        // WHEN
        CartDTO result = cartService.getMyCart();

        // THEN
        assertNotNull(result);
        // 2 ədəd * 80 AZN = 160 AZN
        assertEquals(160.0, result.getTotalAmount());
    }

    @Test
    void removeItem_UgurlaSilinmeli() {
        // GIVEN
        CartItem item = new CartItem();
        item.setId(100L);
        item.setCart(mockCart);

        when(userService.getCurrentUser()).thenReturn(mockUser);
        when(cartRepository.findByUser(mockUser)).thenReturn(Optional.of(mockCart));
        when(itemRepository.findById(100L)).thenReturn(Optional.of(item));

        // WHEN
        assertDoesNotThrow(() -> cartService.removeItem(100L));

        // THEN
        verify(itemRepository, times(1)).delete(item);
    }
}