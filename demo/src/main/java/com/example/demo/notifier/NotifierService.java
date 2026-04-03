package com.example.demo.notifier;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import java.util.HashMap;
import java.util.Map;

@Service
public class NotifierService {

    @Value("${telegram.bot.token}")
    private String token;

    @Value("${telegram.bot.chatId}")
    private String chatId;

    private final RestTemplate restTemplate = new RestTemplate();

    public void enviar(String titulo, String empresa, String ubicacion,
                       String salario, String url) {

        String mensaje = String.format(
                "🎯 *Nueva vacante encontrada*\n\n" +
                        "📌 *%s*\n" +
                        "🏢 %s\n" +
                        "📍 %s\n" +
                        "💰 %s\n" +
                        "🔗 %s",
                titulo, empresa, ubicacion, salario, url
        );

        String apiUrl = "https://api.telegram.org/bot" + token + "/sendMessage";

        // POST con JSON — sin URL encoding manual
        Map<String, String> body = new HashMap<>();
        body.put("chat_id", chatId);
        body.put("text", mensaje);
        body.put("parse_mode", "Markdown");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Map<String, String>> request = new HttpEntity<>(body, headers);

        try {
            restTemplate.postForObject(apiUrl, request, String.class);
            System.out.println("📨 Telegram enviado: " + titulo);
        } catch (Exception e) {
            System.out.println("❌ Error Telegram: " + e.getMessage());
        }
    }
}