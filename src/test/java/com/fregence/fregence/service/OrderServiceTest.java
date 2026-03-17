package com.fregence.fregence.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.ArrayList;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.fregence.fregence.entity.Cart;
import com.fregence.fregence.entity.CartItem;
import com.fregence.fregence.entity.Order;
import com.fregence.fregence.entity.Perfume;
import com.fregence.fregence.entity.User;
import com.fregence.fregence.repository.CartRepository;
import com.fregence.fregence.repository.OrderRepository;

@ExtendWith(MockitoExtension.class)
public class OrderServiceTest {

    @Mock private OrderRepository orderRepository;
    @Mock private CartService cartService;
    @Mock private CartRepository cartRepository;

    @InjectMocks
    private OrderService orderService;

    private Cart mockCart;
    private User mockUser;

    @BeforeEach
    void setUp() {
        mockUser = new User();
        mockUser.setName("Thomas");
        mockUser.setEmail("thomas@mail.com");

        mockCart = new Cart();
        mockCart.setUser(mockUser);
        mockCart.setItems(new ArrayList<>());

        // Səbətə bir ətir əlavə edirik
        Perfume p = new Perfume();
        p.setBrand("Dior");
        p.setName("Sauvage");
        p.setPrice(100.0);

        CartItem item = new CartItem();
        item.setPerfume(p);
        item.setQuantity(2);
        item.setCart(mockCart);
        
        mockCart.getItems().add(item);
    }

    @Test
    void placeOrder_SebetiUgurlaSifarishineCevirmeli() {
        // GIVEN
        when(cartService.getOrCreateCart()).thenReturn(mockCart);
        
        // WHEN
        orderService.placeOrder("Baku, Nizami st.", "0501234567", LocalDateTime.now().plusDays(1), "Zeng edin");

        // THEN
        // 1. OrderRepository.save() metodunun çağırıldığını yoxlayırıq
        verify(orderRepository, times(1)).save(any(Order.class));

        // 2. ƏN VACİB: Səbətin içindəki "items" siyahısının təmizləndiyini yoxlayırıq
        assertTrue(mockCart.getItems().isEmpty(), "Sifarişdən sonra səbət boşalmalı idi!");

        // 3. Səbətin son halının bazada yadda saxlanıldığını yoxlayırıq
        verify(cartRepository, times(1)).save(mockCart);
    }

    @Test
    void placeOrder_SebetBoshdursaXetaVermeli() {
        // GIVEN
        mockCart.setItems(new ArrayList<>()); // Səbəti boşaldırıq
        when(cartService.getOrCreateCart()).thenReturn(mockCart);

        // WHEN & THEN
        RuntimeException ex = assertThrows(RuntimeException.class, () -> {
            orderService.placeOrder("Address", "123", LocalDateTime.now(), "");
        });

        assertEquals("Səbət boşdur!", ex.getMessage());
    }
}