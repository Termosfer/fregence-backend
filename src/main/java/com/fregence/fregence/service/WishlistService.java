package com.fregence.fregence.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fregence.fregence.dto.PerfumeDTO;
import com.fregence.fregence.entity.Perfume;
import com.fregence.fregence.entity.User;
import com.fregence.fregence.entity.Wishlist;
import com.fregence.fregence.repository.PerfumeRepository;
import com.fregence.fregence.repository.WishlistRepository;

@Service
public class WishlistService {

	@Autowired
	private WishlistRepository wishlistRepository;
	@Autowired
	private PerfumeRepository perfumeRepository;
	@Autowired
	private UserService userService;

	public long getWishlistCount() {
		User user = userService.getCurrentUser();
		return wishlistRepository.countByUser(user);
	}

	// 1. Siyahını gətirmək (Xəta verən hissə)
	public List<PerfumeDTO> getMyWishlist() {
		User user = userService.getCurrentUser();
		List<Wishlist> wishes = wishlistRepository.findByUser(user);

		return wishes.stream().map(item -> convertToDto(item.getPerfume())) // Buraya diqqət: {} və return yazmağa
																			// ehtiyac yoxdur
				.toList();
	}

	// 2. Əlavə etmək
	public void addToWishlist(Long perfumeId) {
		User user = userService.getCurrentUser();
		Perfume perfume = perfumeRepository.findById(perfumeId)
				.orElseThrow(() -> new RuntimeException("Ətir tapılmadı"));

		if (!wishlistRepository.existsByUserAndPerfume(user, perfume)) {
			Wishlist wishlist = new Wishlist();
			wishlist.setUser(user);
			wishlist.setPerfume(perfume);
			wishlistRepository.save(wishlist);
		}
	}

	// 3. Silmək
	@Transactional
	public void removeFromWishlist(Long perfumeId) {
		User user = userService.getCurrentUser();
		Perfume perfume = perfumeRepository.findById(perfumeId)
				.orElseThrow(() -> new RuntimeException("Ətir tapılmadı"));
		wishlistRepository.deleteByUserAndPerfume(user, perfume);
	}

	// 4. VACİB: Bu metod mütləq Perfume qəbul etməli və PerfumeDTO qaytarmalıdır!
	// WishlistService daxilindəki convertToDto metodunu belə yenilə:
	private PerfumeDTO convertToDto(Perfume perfume) {
		PerfumeDTO dto = new PerfumeDTO();
		dto.setId(perfume.getId());
		dto.setBrand(perfume.getBrand());
		dto.setName(perfume.getName());
		dto.setImageUrl(perfume.getImageUrl());
		dto.setDescription(perfume.getDescription());
		dto.setIsNew(perfume.getIsNew());
		dto.setIsRecommended(perfume.getIsRecommended());

		if (perfume.getGender() != null) {
			dto.setGender(perfume.getGender().name());
		}

		// VARIANTLARIN MAPPING-I (VACİB!)
		if (perfume.getVariants() != null && !perfume.getVariants().isEmpty()) {
			List<com.fregence.fregence.dto.PerfumeVariantDTO> vDtos = perfume.getVariants().stream().map(v -> {
				com.fregence.fregence.dto.PerfumeVariantDTO vDto = new com.fregence.fregence.dto.PerfumeVariantDTO();
				vDto.setId(v.getId());
				vDto.setMl(v.getMl());
				vDto.setPrice(v.getPrice());
				vDto.setDiscountPrice(v.getDiscountPrice());
				vDto.setStock(v.getStock());

				if (v.getStock() > 0 && v.getStock() <= 3) {
					vDto.setLowStock(true);
					vDto.setStockMessage("Only " + v.getStock() + " left!");
				}
				return vDto;
			}).toList();

			dto.setVariants(vDtos);

			// Başlanğıc qiyməti (minPrice) hesabla
			Double minPrice = vDtos.stream()
					.map(v -> (v.getDiscountPrice() != null && v.getDiscountPrice() > 0) ? v.getDiscountPrice()
							: v.getPrice())
					.min(java.util.Comparator.naturalOrder()).orElse(0.0);

			dto.setPrice(minPrice);
		}

		return dto;
	}
}