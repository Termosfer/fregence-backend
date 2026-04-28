package com.fregence.fregence.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import com.fregence.fregence.dto.OrderItemDTO;
import com.fregence.fregence.dto.OrderResponseDTO;
import com.fregence.fregence.entity.Cart;
import com.fregence.fregence.entity.CartItem;
import com.fregence.fregence.entity.Order;
import com.fregence.fregence.entity.OrderItem;
import com.fregence.fregence.entity.User;
import com.fregence.fregence.repository.CartRepository;
import com.fregence.fregence.repository.OrderItemRepository;
import com.fregence.fregence.repository.OrderRepository;

import jakarta.transaction.Transactional;

@Service
public class OrderService {

	@Autowired
	private OrderRepository orderRepository;

	@Autowired
	private OrderItemRepository orderItemRepository;

	@Autowired
	private TelegramNotificationService telegramService;

	@Autowired
	private SimpMessagingTemplate messagingTemplate;

	@Autowired
	private CartService cartService;

	@Autowired
	private CartRepository cartRepository;

	@Autowired
	private UserService userService;

	@Transactional
	public OrderResponseDTO placeOrder(String address, String phoneNumber, LocalDateTime preferredTime, String note) {
		Cart cart = cartService.getOrCreateCart();
		if (cart.getItems().isEmpty()) {
			throw new RuntimeException("Səbət boşdur!");
		}

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

		List<OrderItem> orderItems = new ArrayList<>();
		double total = 0;

		for (CartItem cartItem : cart.getItems()) {
			OrderItem orderItem = new OrderItem();
			orderItem.setOrder(order);
			orderItem.setPerfume(cartItem.getPerfume());

			String fullName = cartItem.getPerfume().getBrand() + " " + cartItem.getPerfume().getName();
			orderItem.setPerfumeName(fullName);

			// Endirimi yalnız null deyilsə VƏ 0-dan böyükdürsə götür:
			Double price = (cartItem.getPerfume().getDiscountPrice() != null
					&& cartItem.getPerfume().getDiscountPrice() > 0) ? cartItem.getPerfume().getDiscountPrice()
							: cartItem.getPerfume().getPrice();

			orderItem.setPriceAtPurchase(price);
			orderItem.setQuantity(cartItem.getQuantity());

			// YENİ: Satınalma anındakı şəkli qeyd edirik
			orderItem.setImageUrlAtPurchase(cartItem.getPerfume().getImageUrl());

			orderItems.add(orderItem);
			total += price * cartItem.getQuantity();
		}

		order.setOrderItems(orderItems);
		order.setTotalAmount(total);

		Order savedOrder = orderRepository.save(order);

		// VACİB: Telegram bildirişini buraya əlavə edin
		try {
			telegramService.sendOrderNotification(savedOrder);
			messagingTemplate.convertAndSend("/topic/admin-notifications",
					"Yeni sifariş alındı! #" + savedOrder.getId());
		} catch (Exception e) {
			// Sifariş verilməsinə mane olmasın deyə log edirik
			System.err.println("Bildiriş göndərilə bilmədi: " + e.getMessage());
		}

		cart.getItems().clear();
		cartRepository.save(cart);

		return convertToResponseDTO(savedOrder);
	}

	public List<OrderResponseDTO> getAllOrdersForAdmin() {
		return orderRepository.findAll(Sort.by("orderDate").descending()).stream().map(this::convertToResponseDTO)
				.toList();
	}

	// ---- Silmə ----
	@Transactional
	public void deleteOrder(Long orderId) {
		Order order = orderRepository.findById(orderId).orElseThrow(() -> new RuntimeException("Sifariş tapılmadı"));
		orderRepository.delete(order);
	}

	@Transactional
	public void deleteAllOrders() {
		orderRepository.deleteAll();
	}

	// ---- Filtirləmə ----
	public List<OrderResponseDTO> filterOrders(String customerName, Double minPrice, Double maxPrice,
			LocalDateTime startDate, LocalDateTime endDate, String sortBy, String sortDir) {

		Sort sort = sortDir != null && sortDir.equalsIgnoreCase("asc")
				? Sort.by(sortBy != null ? sortBy : "orderDate").ascending()
				: Sort.by(sortBy != null ? sortBy : "orderDate").descending();

		List<Order> orders = orderRepository.findAll(sort);

		return orders.stream().filter(o -> customerName == null
				|| (o.getUser() != null && o.getUser().getName().toLowerCase().contains(customerName.toLowerCase())))
				.filter(o -> minPrice == null || o.getTotalAmount() >= minPrice)
				.filter(o -> maxPrice == null || o.getTotalAmount() <= maxPrice)
				.filter(o -> startDate == null || !o.getOrderDate().isBefore(startDate))
				.filter(o -> endDate == null || !o.getOrderDate().isAfter(endDate)).map(this::convertToResponseDTO)
				.toList();
	}

	@Transactional
	public void updateOrderStatus(Long orderId, String newStatus) {
		Order order = orderRepository.findById(orderId).orElseThrow(() -> new RuntimeException("Sifariş tapılmadı"));
		order.setStatus(newStatus);
		orderRepository.save(order);
	}

	@Transactional
	public void shipOrder(Long orderId, String courierName, String courierPhone, LocalDateTime estimatedTime) {
		Order order = orderRepository.findById(orderId).orElseThrow(() -> new RuntimeException("Sifariş tapılmadı"));

		order.setStatus("SHIPPED");
		order.setCourierName(courierName);
		order.setCourierPhone(courierPhone);
		order.setEstimatedDeliveryTime(estimatedTime);

		orderRepository.save(order);
	}

	// MÜŞTƏRİ ÜÇÜN SİFARİŞ TARİXÇƏSİ
	public List<OrderResponseDTO> getMyOrders() {
		// 1. Hazırda giriş etmiş istifadəçini tapırıq (kiçik hərflə userService)
		User user = userService.getCurrentUser();

		// 2. Həmin istifadəçiyə aid sifarişləri gətiririk
		List<Order> orders = orderRepository.findByUserOrderByOrderDateDesc(user);

		return orders.stream().map(this::convertToResponseDTO).toList();
	}

	private OrderResponseDTO convertToResponseDTO(Order order) {
		OrderResponseDTO dto = new OrderResponseDTO();
		dto.setId(order.getId());
		if (order.getOrderItems() != null && !order.getOrderItems().isEmpty()) {
	        List<OrderItemDTO> itemDtos = order.getOrderItems().stream().map(item -> {
	            OrderItemDTO idto = new OrderItemDTO();
	            idto.setId(item.getId());
	            idto.setPerfumeId(item.getPerfume() != null ? item.getPerfume().getId() : null);
	            idto.setPerfumeName(item.getPerfumeName());
	            
	            // Brend və Şəkil məlumatlarını götürürük
	            if (item.getPerfume() != null) {
	                idto.setBrand(item.getPerfume().getBrand());
	                idto.setImageUrl(item.getPerfume().getImageUrl());
	            }

	            // Qiyməti bazada donmuş (snapshot) dəyərdən götürürük
	            double price = item.getPriceAtPurchase() != null ? item.getPriceAtPurchase() : 0.0;
	            idto.setPrice(price);
	            idto.setQuantity(item.getQuantity());
	            
	            // Subtotal-ı burada yenidən hesablayırıq ki, 0 görsənməsin
	            idto.setSubTotal(price * item.getQuantity());
	            
	            return idto;
			}).toList();
			dto.setItems(itemDtos);
		} else {
			dto.setItems(new ArrayList<>());
		}

		return dto;
	}
}