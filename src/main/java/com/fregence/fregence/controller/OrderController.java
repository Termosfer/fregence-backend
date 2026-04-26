package com.fregence.fregence.controller;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.DeleteMapping;

import com.fregence.fregence.dto.OrderResponseDTO;
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
	 public ResponseEntity<OrderResponseDTO> checkout( // String yerinə OrderResponseDTO yazırıq
	         @RequestParam String address, 
	         @RequestParam String phoneNumber,
	         @RequestParam @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE_TIME) LocalDateTime preferredTime,
	         @RequestParam(required = false) String note) {
	         
	     // Service-dən qayıdan DTO-nu birbaşa ResponseEntity ilə göndəririk
	     OrderResponseDTO orderResponse = orderService.placeOrder(address, phoneNumber, preferredTime, note);
	     return ResponseEntity.ok(orderResponse);
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
	 
	
	
	 // ---- Tək sifariş silmə ----
	 @DeleteMapping("/admin/{id}")
	 public ResponseEntity<String> deleteOrder(@PathVariable Long id) {
	     orderService.deleteOrder(id);
	     return ResponseEntity.ok("Sifariş silindi.");
	 }

	 // ---- Bütün sifarişlər silmə ----
	 @DeleteMapping("/admin/all")
	 public ResponseEntity<String> deleteAllOrders() {
	     orderService.deleteAllOrders();
	     return ResponseEntity.ok("Bütün sifarişlər silindi.");
	 }

	 // ---- Filtirləmə ----
	 @GetMapping("/admin/filter")
	 public ResponseEntity<List<OrderResponseDTO>> filterOrders(
	         @RequestParam(required = false) String customerName,
	         @RequestParam(required = false) Double minPrice,
	         @RequestParam(required = false) Double maxPrice,
	         @RequestParam(required = false) 
	             @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
	         @RequestParam(required = false) 
	             @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
	         @RequestParam(required = false, defaultValue = "orderDate") String sortBy,
	         @RequestParam(required = false, defaultValue = "desc") String sortDir) {

	     return ResponseEntity.ok(
	         orderService.filterOrders(customerName, minPrice, maxPrice, startDate, endDate, sortBy, sortDir)
	     );
	 }
	 
}
