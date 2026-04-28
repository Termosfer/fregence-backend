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
import com.fregence.fregence.entity.User;
import com.fregence.fregence.repository.CartItemRepository;
import com.fregence.fregence.repository.CartRepository;
import com.fregence.fregence.repository.PerfumeRepository;

import jakarta.transaction.Transactional;

@Service
public class CartService {

	@Autowired
	private CartRepository cartRepository;
	@Autowired
	private CartItemRepository itemRepository;
	@Autowired
	private PerfumeRepository perfumeRepository;
	@Autowired
	private UserService userService;

	// 1. Hazırkı istifadəçinin səbətini tap və ya yarat
	public Cart getOrCreateCart() {
		User user = userService.getCurrentUser();
		return cartRepository.findByUser(user).orElseGet(() -> {
			Cart newCart = new Cart();
			newCart.setUser(user);
			return cartRepository.save(newCart);
		});
	}

	// 2. Səbətə məhsul əlavə et
	@Transactional
	public void addToCart(Long perfumeId, int quantity) {
		Cart cart = getOrCreateCart();
		Perfume perfume = perfumeRepository.findById(perfumeId)
				.orElseThrow(() -> new RuntimeException("Perfume not found"));

		// Əgər məhsul artıq səbətdə varsa, sayını artır
		Optional<CartItem> existingItem = cart.getItems().stream()
				.filter(item -> item.getPerfume().getId().equals(perfumeId)).findFirst();

		if (existingItem.isPresent()) {
			CartItem item = existingItem.get();
			item.setQuantity(item.getQuantity() + quantity);
		} else {
			// Yoxdursa, yeni item yarat
			CartItem newItem = new CartItem();
			newItem.setCart(cart);
			newItem.setPerfume(perfume);
			newItem.setQuantity(quantity);
			cart.getItems().add(newItem);
		}
		cartRepository.save(cart);
	}

	// 3. Səbəti göstərmək üçün DTO-ya çevir
	public CartDTO getMyCart() {
	    Cart cart = getOrCreateCart();

	    List<CartItemDTO> itemDtos = cart.getItems().stream().map(item -> {
	        Perfume p = item.getPerfume();
	        
	        // Müştərinin real ödəyəcəyi tək vahid qiyməti tapırıq
	        Double effectivePrice = (p.getDiscountPrice() != null && p.getDiscountPrice() > 0) 
	                                ? p.getDiscountPrice() 
	                                : p.getPrice();

	        return new CartItemDTO(
	            item.getId(),
	            p.getId(),
	            p.getName(),
	            p.getBrand(),
	            p.getPrice(),          // 1. Həmişə orijinal qiyməti göndər
	            p.getDiscountPrice(),   // 2. Varsa endirimli qiyməti göndər
	            item.getQuantity(),
	            effectivePrice * item.getQuantity(), // 3. Cəmi real qiymət üzərindən hesabla
	            p.getImageUrl()
	        );
	    }).toList();

	    // Səbətin ümumi məbləği (bütün subTotal-ların cəmi)
	    Double total = itemDtos.stream().mapToDouble(CartItemDTO::getSubTotal).sum();

	    CartDTO cartDto = new CartDTO();
	    cartDto.setItems(itemDtos);
	    cartDto.setTotalAmount(total);
	    return cartDto;
	}

	// 4. Səbətdən məhsul sil
	@Transactional
	public void removeItem(Long itemId) {
		// 1. Hazırkı login olan istifadəçinin səbətini tapırıq
		Cart cart = getOrCreateCart();

		// 2. Silinmək istəyən məhsulu tapırıq
		CartItem item = itemRepository.findById(itemId)
				.orElseThrow(() -> new RuntimeException("Səbətdə belə bir məhsul tapılmadı."));

		// 3. TƏHLÜKƏSİZLİK Yoxlaması: Bu məhsul həqiqətən bu istifadəçinin
		// səbətindədirmi?
		// Beləliklə, istifadəçi A, istifadəçi B-nin məhsulunu silə bilməz.
		if (!item.getCart().getId().equals(cart.getId())) {
			throw new RuntimeException("Bu məhsulu silmək icazəniz yoxdur!");
		}

		// 4. Hər şey qaydasındadırsa, silirik
		itemRepository.delete(item);
	}
}