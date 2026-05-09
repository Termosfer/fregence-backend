package com.fregence.fregence.controller;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable; // DÜZGÜN İMPORT
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.fregence.fregence.dto.OrderResponseDTO;
import com.fregence.fregence.dto.PagedResponse;
import com.fregence.fregence.service.OrderService;

@RestController
@RequestMapping("/api/orders")
public class OrderController {
    
    @Autowired 
    private OrderService orderService;

    @PatchMapping("/admin/{id}/ship")
    public ResponseEntity<String> shipOrder(
            @PathVariable Long id,
            @RequestParam String courierName,
            @RequestParam String courierPhone,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime estimatedTime) {
        
        orderService.shipOrder(id, courierName, courierPhone, estimatedTime);
        return ResponseEntity.ok("Sifariş kuryerə verildi və status yeniləndi.");
    }
    
    @GetMapping("/my")
    public ResponseEntity<List<OrderResponseDTO>> getMyOrders() {
        return ResponseEntity.ok(orderService.getMyOrders());
    }
    
    @PostMapping("/checkout")
    public ResponseEntity<OrderResponseDTO> checkout(
            @RequestParam String address, 
            @RequestParam String phoneNumber,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime preferredTime,
            @RequestParam(required = false) String note) {
            
        OrderResponseDTO orderResponse = orderService.placeOrder(address, phoneNumber, preferredTime, note);
        return ResponseEntity.ok(orderResponse);
    }
    
    // Admin üçün bütün sifarişlər (Səhifəli)
    @GetMapping("/admin/all")
    public ResponseEntity<PagedResponse<OrderResponseDTO>> getAllOrders(Pageable pageable) {
        return ResponseEntity.ok(orderService.getAllOrdersForAdmin(pageable));
    }

    @PatchMapping("/admin/{id}/status")
    public ResponseEntity<String> updateStatus(@PathVariable Long id, @RequestParam String status) {
        orderService.updateOrderStatus(id, status);
        return ResponseEntity.ok("Sifariş statusu yeniləndi: " + status);
    }
    
    @DeleteMapping("/admin/{id}")
    public ResponseEntity<String> deleteOrder(@PathVariable Long id) {
        orderService.deleteOrder(id);
        return ResponseEntity.ok("Sifariş silindi.");
    }

    @DeleteMapping("/admin/all")
    public ResponseEntity<String> deleteAllOrders() {
        orderService.deleteAllOrders();
        return ResponseEntity.ok("Bütün sifarişlər silindi.");
    }

    // Filtrələmə (Səhifəli)
    @GetMapping("/admin/filter")
    public ResponseEntity<PagedResponse<OrderResponseDTO>> filterOrders(
            @RequestParam(required = false) String customerName,
            @RequestParam(required = false) Double minPrice,
            @RequestParam(required = false) Double maxPrice,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            Pageable pageable) { // Pageable əlavə olundu

        return ResponseEntity.ok(
            orderService.filterOrders(customerName, minPrice, maxPrice, startDate, endDate, pageable)
        );
    }
}