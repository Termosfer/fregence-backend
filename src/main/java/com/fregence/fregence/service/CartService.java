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
	private PerfumeVariantRepository variantRepository;
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

	@Transactional
	public void addToCart(Long variantId, int quantity) {
		Cart cart = getOrCreateCart();

		PerfumeVariant variant = variantRepository.findById(variantId)
				.orElseThrow(() -> new RuntimeException("Product variant not found"));

		if (variant.getStock() < quantity) {
			throw new RuntimeException("Stokda kifayət qədər məhsul yoxdur! Mövcud: " + variant.getStock());
		}

		Optional<CartItem> existingItem = cart.getItems().stream()
				.filter(item -> item.getPerfumeVariant().getId().equals(variantId)).findFirst();

		if (existingItem.isPresent()) {
			CartItem item = existingItem.get();
			int newTotal = item.getQuantity() + quantity;
			if (variant.getStock() < newTotal) {
				throw new RuntimeException("Səbətinizdəki ilə birlikdə stok limitini keçirsiniz!");
			}
			item.setQuantity(newTotal);
		} else {
			CartItem newItem = new CartItem();
			newItem.setCart(cart);
			newItem.setPerfumeVariant(variant);
			newItem.setQuantity(quantity);
			cart.getItems().add(newItem);
		}
		cartRepository.save(cart);
	}

	public CartDTO getMyCart() {
		Cart cart = getOrCreateCart();

		List<CartItemDTO> itemDtos = cart.getItems().stream().map(item -> {
			PerfumeVariant v = item.getPerfumeVariant();
			Perfume p = v.getPerfume();

			Double effectivePrice = (v.getDiscountPrice() != null && v.getDiscountPrice() > 0) ? v.getDiscountPrice()
					: v.getPrice();

			return new CartItemDTO(item.getId(), 
					p.getId(), 
					v.getId(), 
					p.getName(), p.getBrand(), v.getMl(), 
					v.getPrice(), 
					v.getDiscountPrice(), 
					item.getQuantity(), effectivePrice * item.getQuantity(), 
					p.getImageUrl());
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

	// YENİ: Səbətdəki sətir sayını (neçə növ məhsul) qaytaran metod
	public int getCartCount() {
		User user = userService.getCurrentUser();
		return cartRepository.findByUser(user)
				.map(cart -> cart.getItems().size()) // Burada sətir sayını (növ) alırıq
				.orElse(0);
	}
}