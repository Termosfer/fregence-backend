package com.fregence.fregence.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import com.fregence.fregence.dto.OrderItemDTO;
import com.fregence.fregence.dto.OrderResponseDTO;
import com.fregence.fregence.dto.PagedResponse;
import com.fregence.fregence.entity.Cart;
import com.fregence.fregence.entity.CartItem;
import com.fregence.fregence.entity.Order;
import com.fregence.fregence.entity.OrderItem;
import com.fregence.fregence.entity.Perfume;
import com.fregence.fregence.entity.PerfumeVariant;
import com.fregence.fregence.entity.User;
import com.fregence.fregence.repository.CartRepository;
import com.fregence.fregence.repository.OrderRepository;
import com.fregence.fregence.repository.PerfumeVariantRepository;

import jakarta.transaction.Transactional;

@Service
public class OrderService {

    @Autowired private OrderRepository orderRepository;
    @Autowired private PerfumeVariantRepository variantRepository;
    @Autowired private TelegramNotificationService telegramService;
    @Autowired private SimpMessagingTemplate messagingTemplate;
    @Autowired private CartService cartService;
    @Autowired private CartRepository cartRepository;
    @Autowired private UserService userService;

    // 1. SİFARİŞ YARATMAQ (Status: AWAITING_PAYMENT)
    @Transactional
    public OrderResponseDTO placeOrder(String address, String phoneNumber, LocalDateTime preferredTime, String note) {
        User user = userService.getCurrentUser();
        Cart cart = cartService.getOrCreateCart();

        if (cart.getItems().isEmpty()) {
            throw new RuntimeException("Səbətiniz boşdur!");
        }

        if (preferredTime != null && preferredTime.isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Çatdırılma vaxtı keçmiş tarix ola bilməz!");
        }

        Order order = new Order();
        order.setUser(user);
        order.setAddress(address);
        order.setPhoneNumber(phoneNumber);
        order.setPreferredDeliveryTime(preferredTime);
        order.setOrderNote(note);
        order.setOrderDate(LocalDateTime.now());
        
        // YENİ: İlk status ödəniş gözlənilir olaraq təyin edilir
        order.setStatus("AWAITING_PAYMENT");

        List<OrderItem> orderItems = new ArrayList<>();
        double total = 0;

        for (CartItem cartItem : cart.getItems()) {
            PerfumeVariant variant = cartItem.getPerfumeVariant();
            Perfume p = variant.getPerfume();

            if (variant.getStock() < cartItem.getQuantity()) {
                throw new RuntimeException("Stok çatışmır: " + p.getName());
            }
            variant.setStock(variant.getStock() - cartItem.getQuantity());
            variantRepository.save(variant);

            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(order);
            orderItem.setPerfumeVariant(variant); 
            orderItem.setPerfumeName(p.getName());
            orderItem.setBrandName(p.getBrand());
            orderItem.setMl(variant.getMl());
            orderItem.setQuantity(cartItem.getQuantity());
            orderItem.setImageUrlAtPurchase(p.getImageUrl());

            Double price = (variant.getDiscountPrice() != null && variant.getDiscountPrice() > 0) 
                           ? variant.getDiscountPrice() : variant.getPrice();
            orderItem.setPriceAtPurchase(price);

            orderItems.add(orderItem);
            total += price * cartItem.getQuantity();
        }

        order.setOrderItems(orderItems);
        order.setTotalAmount(total);
        Order savedOrder = orderRepository.save(order);

        try {
            telegramService.sendOrderNotification(savedOrder);
            messagingTemplate.convertAndSend("/topic/admin-notifications", "Yeni sifariş! #" + savedOrder.getId());
        } catch (Exception e) {
            System.err.println("Bildiriş xətası: " + e.getMessage());
        }

        cart.getItems().clear();
        cartRepository.save(cart);

        return convertToResponseDTO(savedOrder);
    }

    // 2. ÖDƏNİŞİ TƏSDİQLƏMƏK (PaymentController üçün)
    @Transactional
    public void confirmPayment(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Sifariş tapılmadı"));
        
        if (order.getStatus().equals("AWAITING_PAYMENT")) {
            order.setStatus("PAID");
            orderRepository.save(order);
        }
    }

    // 3. STATUS KEÇİD QAYDALARI (Validation)
    @Transactional
    public void updateOrderStatus(Long orderId, String newStatus) {
        Order order = orderRepository.findById(orderId).orElseThrow(() -> new RuntimeException("Sifariş tapılmadı"));
        
        String current = order.getStatus();
        String next = newStatus.toUpperCase();

        // QAYDA: Tamamlanmış və ya ləğv edilmiş sifariş geri qayıda bilməz
        if (current.equals("DELIVERED") || current.equals("CANCELLED")) {
            throw new RuntimeException("Bu sifariş artıq yekunlaşıb, statusu dəyişdirilə bilməz.");
        }

        // QAYDA: Ödənişi edilməmiş mal yola çıxa bilməz
        if (current.equals("AWAITING_PAYMENT") && next.equals("SHIPPED")) {
            throw new RuntimeException("Ödənişi tamamlanmamış sifariş kuryerə verilə bilməz!");
        }

        order.setStatus(next);
        orderRepository.save(order);
    }

    // 4. SOFT DELETE (Yumşaq Silmə)
    @Transactional
    public void deleteOrder(Long orderId) {
        Order order = orderRepository.findById(orderId).orElseThrow(() -> new RuntimeException("Sifariş tapılmadı"));
        
        order.setDeleted(true); // Bazadan silmirik, sadəcə işarələyirik
        order.setStatus("CANCELLED"); // Silinən sifarişi ləğv edilmiş kimi qeyd edirik
        orderRepository.save(order);
    }

    // 5. MÜŞTƏRİ TARİXÇƏSİ (Yalnız silinməyənlər)
    public List<OrderResponseDTO> getMyOrders() {
        User user = userService.getCurrentUser();
        // Repository-də mütləq findByUserAndIsDeletedFalseOrderByOrderDateDesc metodu olmalıdır
        return orderRepository.findByUserAndIsDeletedFalseOrderByOrderDateDesc(user)
                .stream().map(this::convertToResponseDTO).toList();
    }

    // 6. ADMİN: BÜTÜN SİFARİŞLƏR (Silinməyənlər)
    public PagedResponse<OrderResponseDTO> getAllOrdersForAdmin(Pageable pageable) {
        // Repository-də findAllByIsDeletedFalse metodu olmalıdır
        Page<Order> ordersPage = orderRepository.findAllByIsDeletedFalse(pageable);
        
        List<OrderResponseDTO> dtoList = ordersPage.getContent().stream()
                .map(this::convertToResponseDTO)
                .toList();

        return new PagedResponse<>(
                dtoList,
                ordersPage.getNumber(),
                ordersPage.getSize(),
                ordersPage.getTotalElements(),
                ordersPage.getTotalPages(),
                ordersPage.isLast()
        );
    }

    // 7. ADMİN: FİLTİRLƏMƏ
    public PagedResponse<OrderResponseDTO> filterOrders(String customerName, Double minPrice, Double maxPrice,
            LocalDateTime startDate, LocalDateTime endDate, Pageable pageable) {

        List<OrderResponseDTO> allFiltered = orderRepository.findAllByIsDeletedFalse(Sort.by(Sort.Direction.DESC, "orderDate")).stream()
                .filter(o -> customerName == null || 
                       (o.getUser() != null && o.getUser().getName().toLowerCase().contains(customerName.toLowerCase())))
                .filter(o -> minPrice == null || o.getTotalAmount() >= minPrice)
                .filter(o -> maxPrice == null || o.getTotalAmount() <= maxPrice)
                .filter(o -> startDate == null || !o.getOrderDate().isBefore(startDate))
                .filter(o -> endDate == null || !o.getOrderDate().isAfter(endDate))
                .map(this::convertToResponseDTO)
                .toList();

        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), allFiltered.size());
        
        List<OrderResponseDTO> pagedContent = new ArrayList<>();
        if (start < allFiltered.size()) {
            pagedContent = allFiltered.subList(start, end);
        }

        return new PagedResponse<>(
                pagedContent,
                pageable.getPageNumber(),
                pageable.getPageSize(),
                (long) allFiltered.size(),
                (int) Math.ceil((double) allFiltered.size() / pageable.getPageSize()),
                end >= allFiltered.size()
        );
    }

    // ADMİN: KURYER TƏYİNİ (Təkmilləşdirilmiş)
    @Transactional
    public void shipOrder(Long orderId, String courierName, String courierPhone, LocalDateTime estimatedTime) {
        Order order = orderRepository.findById(orderId).orElseThrow(() -> new RuntimeException("Sifariş tapılmadı"));

        if (order.getStatus().equals("AWAITING_PAYMENT")) {
            throw new RuntimeException("Ödənişi edilməmiş sifarişi kuryerə verə bilməzsiniz!");
        }

        order.setStatus("SHIPPED");
        order.setCourierName(courierName);
        order.setCourierPhone(courierPhone);
        order.setEstimatedDeliveryTime(estimatedTime);
        orderRepository.save(order);
    }

    @Transactional
    public void deleteAllOrders() {
        // Professional yanaşmada hamısını silmək əvəzinə hamısını Deleted etmək olar
        List<Order> all = orderRepository.findAll();
        all.forEach(o -> o.setDeleted(true));
        orderRepository.saveAll(all);
    }

    // --- ENTITY -> DTO ÇEVİRMƏ ---
    private OrderResponseDTO convertToResponseDTO(Order order) {
        OrderResponseDTO dto = new OrderResponseDTO();
        dto.setId(order.getId());
        dto.setTotalAmount(order.getTotalAmount());
        dto.setAddress(order.getAddress());
        dto.setPhoneNumber(order.getPhoneNumber());
        dto.setOrderNote(order.getOrderNote());
        dto.setStatus(order.getStatus());
        dto.setOrderDate(order.getOrderDate());
        dto.setPreferredDeliveryTime(order.getPreferredDeliveryTime());
        
        if (order.getUser() != null) {
            dto.setCustomerName(order.getUser().getName()); 
            dto.setCustomerEmail(order.getUser().getEmail());
        }

        dto.setCourierName(order.getCourierName());
        dto.setCourierPhone(order.getCourierPhone());
        dto.setEstimatedDeliveryTime(order.getEstimatedDeliveryTime());

        List<OrderItemDTO> itemDtos = order.getOrderItems().stream().map(item -> {
            OrderItemDTO iDto = new OrderItemDTO();
            iDto.setId(item.getId());
            if (item.getPerfumeVariant() != null) {
                iDto.setPerfumeId(item.getPerfumeVariant().getPerfume().getId());
                iDto.setBrand(item.getBrandName());
            }
            iDto.setPerfumeName(item.getPerfumeName());
            iDto.setPrice(item.getPriceAtPurchase());
            iDto.setQuantity(item.getQuantity());
            iDto.setSubTotal(item.getPriceAtPurchase() * item.getQuantity());
            iDto.setImageUrl(item.getImageUrlAtPurchase());
            return iDto;
        }).collect(Collectors.toList());

        dto.setItems(itemDtos);
        return dto;
    }
}