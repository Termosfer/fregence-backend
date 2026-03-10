package com.fregence.fregence.controller;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fregence.fregence.dto.OrderResponseDTO;
import com.fregence.fregence.service.OrderService;
@RestController
@RequestMapping("/api/orders")
public class OrderController {
	 @Autowired 
	 private OrderService orderService;

	 @PostMapping("/checkout")
	 public ResponseEntity<String> checkout(
	         @RequestParam String address, 
	         @RequestParam String phoneNumber,
	         @RequestParam @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE_TIME) LocalDateTime preferredTime,
	         @RequestParam(required = false) String note) {
	         
	     orderService.placeOrder(address, phoneNumber, preferredTime, note);
	     return ResponseEntity.ok("Sifarişiniz uğurla qəbul edildi!");
	 }
	 
	// Admin üçün bütün sifarişlər
	 @GetMapping("/admin/all")
	 public ResponseEntity<List<OrderResponseDTO>> getAllOrders() {
	     return ResponseEntity.ok(orderService.getAllOrdersForAdmin());
	 }

	 // Admin üçün status yeniləmə (Məs: /api/orders/admin/1/status?status=COMPLETED)
	 @PatchMapping("/admin/{id}/status")
	 public ResponseEntity<String> updateStatus(@PathVariable Long id, @RequestParam String status) {
	     orderService.updateOrderStatus(id, status);
	     return ResponseEntity.ok("Sifariş statusu yeniləndi: " + status);
	 }
}
