package com.fregence.fregence.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fregence.fregence.dto.OrderItemDTO;
import com.fregence.fregence.dto.OrderResponseDTO;
import com.fregence.fregence.entity.Cart;
import com.fregence.fregence.entity.CartItem;
import com.fregence.fregence.entity.Order;
import com.fregence.fregence.entity.OrderItem;
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

        if (preferredTime != null && preferredTime.isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Çatdırılma vaxtı keçmiş tarix ola bilməz!");
        }

        // 1. Sifariş obyektini yaradırıq
        Order order = new Order();
        order.setUser(cart.getUser());
        order.setAddress(address);
        order.setPhoneNumber(phoneNumber);
        order.setPreferredDeliveryTime(preferredTime); 
        order.setOrderNote(note);
        order.setOrderDate(LocalDateTime.now());
        order.setStatus("PENDING");

        // 2. Məhsulları köçürmə məntiqi (BURA ƏLAVƏ EDİLDİ)
        List<OrderItem> orderItems = new ArrayList<>();
        double total = 0;

        for (CartItem cartItem : cart.getItems()) {
            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(order); // Vacib: Item-i sifarişə bağlayırıq
            orderItem.setPerfume(cartItem.getPerfume());
            
            // Adı və qiyməti "dondururuq" (Snapshot)
            String fullName = cartItem.getPerfume().getBrand() + " " + cartItem.getPerfume().getName();
            orderItem.setPerfumeName(fullName);
            
            Double price = (cartItem.getPerfume().getDiscountPrice() != null) ? 
                            cartItem.getPerfume().getDiscountPrice() : cartItem.getPerfume().getPrice();
            
            orderItem.setPriceAtPurchase(price);
            orderItem.setQuantity(cartItem.getQuantity());

            orderItems.add(orderItem);
            total += price * cartItem.getQuantity();
        }

        // 3. Sifarişin içini doldururuq
        order.setOrderItems(orderItems);
        order.setTotalAmount(total);

        // 4. Sifarişi bazaya yazırıq
        orderRepository.save(order);
        
        // 5. Səbəti təmizləyirik
        cart.getItems().clear();
        cartRepository.save(cart);
    }
    
    public List<OrderResponseDTO> getAllOrdersForAdmin() {
        return orderRepository.findAll(org.springframework.data.domain.Sort.by("orderDate").descending())
                .stream()
                .map(this::convertToResponseDTO)
                .toList();
    }

    @Transactional
    public void updateOrderStatus(Long orderId, String newStatus) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Sifariş tapılmadı"));
        order.setStatus(newStatus);
        orderRepository.save(order);
    }

    @Transactional
    public void shipOrder(Long orderId, String courierName, String courierPhone, LocalDateTime estimatedTime) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Sifariş tapılmadı"));

        order.setStatus("SHIPPED"); // Statusu avtomatik yola düşdü edirik
        order.setCourierName(courierName);
        order.setCourierPhone(courierPhone);
        order.setEstimatedDeliveryTime(estimatedTime);

        orderRepository.save(order);
        
        // Gələcəkdə bura müştəriyə "Kuryer yoldadır" emaili göndərməyi də əlavə edə bilərsən
    }
    
    
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

        // Kuryer məlumatlarını bura əlavə edirik:
        dto.setCourierName(order.getCourierName());
        dto.setCourierPhone(order.getCourierPhone());
        dto.setEstimatedDeliveryTime(order.getEstimatedDeliveryTime());
        
        // Item-ləri DTO-ya çeviririk
        if (order.getOrderItems() != null) {
            List<OrderItemDTO> itemDtos = order.getOrderItems().stream().map(item -> {
                OrderItemDTO idto = new OrderItemDTO();
                idto.setId(item.getId());
                idto.setPerfumeId(item.getPerfume() != null ? item.getPerfume().getId() : null);
                idto.setPerfumeName(item.getPerfumeName());
                
                // BURA DIQQƏT: Brend və Şəkli 'Perfume' obyektindən götürürük
                if (item.getPerfume() != null) {
                    idto.setBrand(item.getPerfume().getBrand());
                    idto.setImageUrl(item.getPerfume().getImageUrl());
                }

                idto.setPrice(item.getPriceAtPurchase());
                idto.setQuantity(item.getQuantity());
                idto.setSubTotal(item.getPriceAtPurchase() * item.getQuantity());
                return idto;
            }).toList();
            dto.setItems(itemDtos);
        } else {
            dto.setItems(new ArrayList<>());
        }
        
        return dto;
    }
}