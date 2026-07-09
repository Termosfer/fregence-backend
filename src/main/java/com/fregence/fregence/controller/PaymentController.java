package com.fregence.fregence.controller;

import com.fregence.fregence.entity.Cart;
import com.fregence.fregence.entity.PerfumeVariant;
import com.fregence.fregence.entity.User;
import com.fregence.fregence.repository.CartRepository;
import com.fregence.fregence.service.UserService;
import com.stripe.Stripe;
import com.stripe.model.PaymentIntent;
import com.stripe.param.PaymentIntentCreateParams;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/payment")
public class PaymentController {

    @Value("${STRIPE_SECRET_KEY}")
    private String stripeSecretKey;

    // --- BURADA ENJEKSİYA EDİ̇Rİ̇K (Xətaları həll edən hissə) ---
    @Autowired 
    private UserService userService;

    @Autowired 
    private CartRepository cartRepository;

    @PostMapping("/create-payment-intent")
    public ResponseEntity<Map<String, String>> createPaymentIntent() throws Exception {
        // 1. Hazırda sistemə giriş etmiş istifadəçini tapırıq
        User user = userService.getCurrentUser();
        
        // 2. İstifadəçinin səbətini tapırıq
        Cart cart = cartRepository.findByUser(user)
                .orElseThrow(() -> new RuntimeException("Səbət tapılmadı"));
            
        // 3. Məbləği Backend-də hesablayırıq (Təhlükəsizlik üçün!)
        double totalAmount = cart.getItems().stream()
                .mapToDouble(item -> {
                    PerfumeVariant v = item.getPerfumeVariant();
                    // Endirimli qiymət varsa onu, yoxdursa normal qiyməti götürürük
                    double price = (v.getDiscountPrice() != null && v.getDiscountPrice() > 0) 
                            ? v.getDiscountPrice() 
                            : v.getPrice();
                    return price * item.getQuantity();
                }).sum();

        // 4. Stripe tənzimləmələri
        Stripe.apiKey = stripeSecretKey;

        // Stripe məbləği tam ədəd (qəpik/cent) kimi gözləyir
        long amountInCents = (long) (totalAmount * 100);

        PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
                .setAmount(amountInCents)
                .setCurrency("azn")
                .setAutomaticPaymentMethods(
                        PaymentIntentCreateParams.AutomaticPaymentMethods.builder()
                                .setEnabled(true)
                                .build()
                )
                .build();

        PaymentIntent intent = PaymentIntent.create(params);

        Map<String, String> response = new HashMap<>();
        response.put("clientSecret", intent.getClientSecret());
        
        return ResponseEntity.ok(response);
    }
}