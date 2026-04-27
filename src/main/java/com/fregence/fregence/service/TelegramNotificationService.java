package com.fregence.fregence.service;

import com.fregence.fregence.entity.Order;
import com.fregence.fregence.entity.OrderItem;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@Service
public class TelegramNotificationService {

    @Value("${app.telegram.token}")
    private String botToken;

    @Value("${app.telegram.chat-id}")
    private String chatId;

    @Value("${app.admin.url}")
    private String adminBaseUrl;

    public void sendOrderNotification(Order order) {
        String fullAdminLink = adminBaseUrl + "/admin/orders";

        // 1. Məhsulların siyahısını hazırlayırıq
        StringBuilder itemsBuilder = new StringBuilder();
        if (order.getOrderItems() != null) {
            for (OrderItem item : order.getOrderItems()) {
                itemsBuilder.append(String.format("• %s (<b>%d ədəd</b>)\n", 
                    item.getPerfumeName(), 
                    item.getQuantity()));
            }
        }

        // 2. Mesajın tam strukturu
        String message = String.format(
            "<b>📦 YENİ SİFARİŞ ALINDI! #%d</b>\n\n" +
            "<b>🛍 Məhsullar:</b>\n%s\n" + // Məhsul siyahısı bura gəlir
            "<b>👤 Müştəri:</b> %s\n" +
            "<b>📞 Telefon:</b> %s\n" +
            "<b>💰 Toplam Məbləğ:</b> %.2f AZN\n" +
            "<b>📍 Ünvan:</b> %s\n\n" +
            "<b>📝 Qeyd:</b> <i>%s</i>\n\n" +
            "🔗 <a href=\"%s\">Admin Paneldə İdarə Et</a>\n\n" +
            "🚀 <i>Zəhmət olmasa, kuryer təyin edin.</i>",
            order.getId(),
            itemsBuilder.toString(), // Məhsullar
            (order.getUser() != null ? order.getUser().getName() : "Qonaq"),
            order.getPhoneNumber(),
            order.getTotalAmount(),
            order.getAddress(),
            (order.getOrderNote() != null && !order.getOrderNote().isEmpty() ? order.getOrderNote() : "Yoxdur"),
            fullAdminLink
        );

        String url = "https://api.telegram.org/bot" + botToken + "/sendMessage";
        
        try {
            UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(url)
                    .queryParam("chat_id", chatId)
                    .queryParam("text", message)
                    .queryParam("parse_mode", "HTML");

            RestTemplate restTemplate = new RestTemplate();
            restTemplate.getForEntity(builder.build().toUri(), String.class);
            System.out.println("Məhsul detalları ilə Telegram bildirişi göndərildi.");
        } catch (Exception e) {
            System.err.println("Telegram xətası: " + e.getMessage());
        }
    }
}