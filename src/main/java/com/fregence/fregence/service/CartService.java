package com.fregence.fregence.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fregence.fregence.dto.CartDTO;
import com.fregence.fregence.dto.CartItemDTO;
import com.fregence.fregence.entity.Cart;
import com.fregence.fregence.entity.CartItem;
import com.fregence.fregence.entity.Perfume;
import com.fregence.fregence.entity.PerfumeVariant;
import com.fregence.fregence.entity.User;
import com.fregence.fregence.repository.CartItemRepository;
import com.fregence.fregence.repository.CartRepository;
import com.fregence.fregence.repository.PerfumeVariantRepository;

import jakarta.transaction.Transactional;

@Service
public class CartService {

	@Autowired
	private CartRepository cartRepository;
	@Autowired
	private CartItemRepository itemRepository;
	@Autowired
	private PerfumeVariantRepository variantRepository; // Yeni əlavə olundu
	@Autowired
	private UserService userService;

	public Cart getOrCreateCart() {
		User user = userService.getCurrentUser();
		return cartRepository.findByUser(user).orElseGet(() -> {
			Cart newCart = new Cart();
			newCart.setUser(user);
			return cartRepository.save(newCart);
		});
	}

	// 2. Səbətə konkret VARIANT (ölçü) əlavə et
	@Transactional
	public void addToCart(Long variantId, int quantity) {
		Cart cart = getOrCreateCart();
		
		// Variantı tapırıq (qiymət və ml buradadır)
		PerfumeVariant variant = variantRepository.findById(variantId)
				.orElseThrow(() -> new RuntimeException("Product variant not found"));

		// Səbətdə bu konkret variant (məs: eyni ətirin 50ml-i) varmı?
		Optional<CartItem> existingItem = cart.getItems().stream()
				.filter(item -> item.getPerfumeVariant().getId().equals(variantId)).findFirst();

		if (existingItem.isPresent()) {
			CartItem item = existingItem.get();
			item.setQuantity(item.getQuantity() + quantity);
		} else {
			CartItem newItem = new CartItem();
			newItem.setCart(cart);
			newItem.setPerfumeVariant(variant);
			newItem.setQuantity(quantity);
			cart.getItems().add(newItem);
		}
		cartRepository.save(cart);
	}

	// 3. Səbəti göstərmək (Variant məlumatları ilə)
	public CartDTO getMyCart() {
	    Cart cart = getOrCreateCart();

	    List<CartItemDTO> itemDtos = cart.getItems().stream().map(item -> {
	        PerfumeVariant v = item.getPerfumeVariant();
	        Perfume p = v.getPerfume();
	        
	        // Real qiyməti tapırıq (Endirim 0-dan böyükdürsə onu götür)
	        Double effectivePrice = (v.getDiscountPrice() != null && v.getDiscountPrice() > 0) 
	                                ? v.getDiscountPrice() 
	                                : v.getPrice();

	        return new CartItemDTO(
	            item.getId(),       // cartItemId
	            p.getId(),          // perfumeId
	            v.getId(),          // variantId
	            p.getName(),
	            p.getBrand(),
	            v.getMl(),          // Ölçü (30, 50, 100...)
	            v.getPrice(),       // Orijinal qiymət
	            v.getDiscountPrice(),// Endirimli qiymət
	            item.getQuantity(),
	            effectivePrice * item.getQuantity(), // Alt cəm
	            p.getImageUrl()
	        );
	    }).toList();

	    Double total = itemDtos.stream().mapToDouble(CartItemDTO::getSubTotal).sum();

	    CartDTO cartDto = new CartDTO();
	    cartDto.setItems(itemDtos);
	    cartDto.setTotalAmount(total);
	    return cartDto;
	}

	@Transactional
	public void removeItem(Long itemId) {
		Cart cart = getOrCreateCart();
		CartItem item = itemRepository.findById(itemId)
				.orElseThrow(() -> new RuntimeException("Səbətdə belə bir məhsul tapılmadı."));

		if (!item.getCart().getId().equals(cart.getId())) {
			throw new RuntimeException("Bu məhsulu silmək icazəniz yoxdur!");
		}

		itemRepository.delete(item);
	}
}