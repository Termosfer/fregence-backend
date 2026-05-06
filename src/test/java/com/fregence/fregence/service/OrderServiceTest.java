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
import org.springframework.messaging.simp.SimpMessagingTemplate;

import com.fregence.fregence.dto.OrderResponseDTO;
import com.fregence.fregence.entity.*;
import com.fregence.fregence.repository.CartRepository;
import com.fregence.fregence.repository.OrderRepository;

@ExtendWith(MockitoExtension.class)
public class OrderServiceTest {

    @Mock private OrderRepository orderRepository;
    @Mock private CartService cartService;
    @Mock private CartRepository cartRepository;
    @Mock private UserService userService;
    @Mock private TelegramNotificationService telegramService; // Lazımdırsa
    @Mock private SimpMessagingTemplate messagingTemplate; // WebSocket üçün

    @InjectMocks
    private OrderService orderService;

    private Cart mockCart;
    private User mockUser;
    private PerfumeVariant mockVariant;

    @BeforeEach
    void setUp() {
        mockUser = new User();
        mockUser.setName("Thomas");
        mockUser.setEmail("thomas@mail.com");

        mockCart = new Cart();
        mockCart.setUser(mockUser);
        mockCart.setItems(new ArrayList<>());

        Perfume p = new Perfume();
        p.setBrand("Dior");
        p.setName("Sauvage");

        mockVariant = new PerfumeVariant();
        mockVariant.setId(1L);
        mockVariant.setPrice(100.0);
        mockVariant.setMl(100);
        mockVariant.setPerfume(p);

        CartItem item = new CartItem();
        item.setPerfumeVariant(mockVariant);
        item.setQuantity(2);
        item.setCart(mockCart);
        
        mockCart.getItems().add(item);
    }

    @Test
    void placeOrder_SebetiUgurlaSifarisheCevirmeli() {
        // GIVEN
        when(cartService.getOrCreateCart()).thenReturn(mockCart);
        // SavedOrder-i qaytarmaq üçün mock
        when(orderRepository.save(any(Order.class))).thenAnswer(i -> i.getArguments()[0]);
        
        // WHEN
        OrderResponseDTO result = orderService.placeOrder("Address", "123", null, "note");

        // THEN
        assertNotNull(result);
        assertTrue(mockCart.getItems().isEmpty());
        verify(orderRepository, times(1)).save(any(Order.class));
    }

    @Test
    void placeOrder_SebetBoshdursaXetaVermeli() {
        // GIVEN
        mockCart.setItems(new ArrayList<>());
        when(cartService.getOrCreateCart()).thenReturn(mockCart);

        // WHEN & THEN
        RuntimeException ex = assertThrows(RuntimeException.class, () -> {
            orderService.placeOrder("Address", "123", null, "");
        });

        assertEquals("Səbət boşdur!", ex.getMessage());
    }
}