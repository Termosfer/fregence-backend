package com.fregence.fregence.service;


import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fregence.fregence.dto.OrderItemDTO;
import com.fregence.fregence.dto.OrderResponseDTO;
import com.fregence.fregence.entity.Cart;
import com.fregence.fregence.entity.Order;
import com.fregence.fregence.repository.CartRepository;
import com.fregence.fregence.repository.OrderRepository;

import jakarta.transaction.Transactional;

@Service
public class OrderService {

    
	@Autowired
	private OrderRepository orderRepository;
	@Autowired
	private CartService cartService;
	@Autowired
	private CartRepository cartRepository;

    

	@Transactional
	public void placeOrder(String address, String phoneNumber, LocalDateTime preferredTime, String note) {
	    Cart cart = cartService.getOrCreateCart();
	    if (cart.getItems().isEmpty()) throw new RuntimeException("Səbət boşdur!");

	    // Sadə bir yoxlama: Seçilən çatdırılma vaxtı keçmişdə olmamalıdır
	    if (preferredTime != null && preferredTime.isBefore(LocalDateTime.now())) {
	        throw new RuntimeException("Çatdırılma vaxtı keçmiş tarix ola bilməz!");
	    }

	    Order order = new Order();
	    order.setUser(cart.getUser());
	    order.setAddress(address);
	    order.setPhoneNumber(phoneNumber);
	    order.setPreferredDeliveryTime(preferredTime); 
	    order.setOrderNote(note);
	    order.setOrderDate(LocalDateTime.now());
	    order.setStatus("PENDING");

	    // ... (OrderItem-ləri köçürmə məntiqi eyni qalır) ...
	    
	    orderRepository.save(order);
	    
	    // Səbəti təmizlə
	    cart.getItems().clear();
	    cartRepository.save(cart);
	}
	
	// 1. Bütün sifarişləri gətir (Admin üçün)
	public List<OrderResponseDTO> getAllOrdersForAdmin() {
	    // Sifarişləri tarixinə görə (ən yeni birinci) gətiririk
	    return orderRepository.findAll(org.springframework.data.domain.Sort.by("orderDate").descending())
	            .stream()
	            .map(this::convertToResponseDTO)
	            .toList();
	}

	// 2. Sifarişin statusunu dəyiş (Məs: PENDING -> SHIPPED)
	@Transactional
	public void updateOrderStatus(Long orderId, String newStatus) {
	    Order order = orderRepository.findById(orderId)
	            .orElseThrow(() -> new RuntimeException("Sifariş tapılmadı"));
	    order.setStatus(newStatus);
	    orderRepository.save(order);
	}

	// Köməkçi metod (Entity-ni DTO-ya çevirmək üçün)
	private OrderResponseDTO convertToResponseDTO(Order order) {
	    OrderResponseDTO dto = new OrderResponseDTO();
	    dto.setId(order.getId());
	    dto.setCustomerName(order.getUser().getName());
	    dto.setCustomerEmail(order.getUser().getEmail());
	    dto.setTotalAmount(order.getTotalAmount());
	    dto.setAddress(order.getAddress());
	    dto.setPhoneNumber(order.getPhoneNumber());
	    dto.setOrderNote(order.getOrderNote());
	    dto.setStatus(order.getStatus());
	    dto.setOrderDate(order.getOrderDate());
	    dto.setPreferredDeliveryTime(order.getPreferredDeliveryTime());

	    List<OrderItemDTO> itemDtos = order.getOrderItems().stream().map(item -> 
	        new OrderItemDTO(item.getId(), item.getPerfume().getId(), item.getPerfumeName(), null, item.getPriceAtPurchase(), item.getQuantity(), item.getPriceAtPurchase() * item.getQuantity())
	    ).toList();
	    
	    dto.setItems(itemDtos);
	    return dto;
	}
}
