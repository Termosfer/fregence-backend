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
import com.fregence.fregence.entity.User;
import com.fregence.fregence.repository.CartItemRepository;
import com.fregence.fregence.repository.CartRepository;
import com.fregence.fregence.repository.PerfumeRepository;

@ExtendWith(MockitoExtension.class)
public class CartServiceTest {

    @Mock private CartRepository cartRepository;
    @Mock private CartItemRepository itemRepository;
    @Mock private PerfumeRepository perfumeRepository;
    @Mock private UserService userService;

    @InjectMocks
    private CartService cartService;

    private User mockUser;
    private Cart mockCart;
    private Perfume mockPerfume;

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
        mockPerfume.setPrice(100.0);
        mockPerfume.setDiscountPrice(80.0); // Endirimli qiymət
    }

    @Test
    void addToCart_YeniMehsulElaveEdilmeli() {
        // GIVEN
        when(userService.getCurrentUser()).thenReturn(mockUser);
        when(cartRepository.findByUser(mockUser)).thenReturn(Optional.of(mockCart));
        when(perfumeRepository.findById(1L)).thenReturn(Optional.of(mockPerfume));

        // WHEN
        cartService.addToCart(1L, 2);

        // THEN
        assertEquals(1, mockCart.getItems().size());
        assertEquals(2, mockCart.getItems().get(0).getQuantity());
        verify(cartRepository, times(1)).save(mockCart);
    }

    @Test
    void getMyCart_HesablamaMentiqiDuzgunIshlemeli() {
        // GIVEN
        // Səbətə bir item əlavə edirik: 2 ədəd Sauvage (hərəsi 80 AZN)
        CartItem item = new CartItem();
        item.setPerfume(mockPerfume);
        item.setQuantity(2);
        item.setCart(mockCart);
        mockCart.getItems().add(item);

        when(userService.getCurrentUser()).thenReturn(mockUser);
        when(cartRepository.findByUser(mockUser)).thenReturn(Optional.of(mockCart));

        // WHEN
        CartDTO result = cartService.getMyCart();

        // THEN
        assertNotNull(result);
        // 2 ədəd * 80 AZN = 160 AZN olmalıdır
        assertEquals(160.0, result.getTotalAmount());
        assertEquals(1, result.getItems().size());
        assertEquals(160.0, result.getItems().get(0).getSubTotal());
    }

    @Test
    void removeFromCart_SadeceOzMehsulunuSileBilmelisiniz() {
        // GIVEN
        CartItem item = new CartItem();
        item.setId(100L);
        item.setCart(mockCart); // Bu item mockCart-a (bizim userə) aiddir

        when(userService.getCurrentUser()).thenReturn(mockUser);
        when(cartRepository.findByUser(mockUser)).thenReturn(Optional.of(mockCart));
        when(itemRepository.findById(100L)).thenReturn(Optional.of(item));

        // WHEN & THEN (Heç bir xəta çıxmamalıdır)
        assertDoesNotThrow(() -> cartService.removeItem(100L));
        verify(itemRepository, times(1)).delete(item);
    }
}