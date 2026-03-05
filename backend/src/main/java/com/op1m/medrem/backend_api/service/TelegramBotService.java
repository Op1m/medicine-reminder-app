package com.op1m.medrem.backend_api.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Service
public class TelegramBotService {

    @Value("${telegram.bot.token}")
    private String botToken;

    private final RestTemplate restTemplate = new RestTemplate();
    private static final String TELEGRAM_API_URL = "https://api.telegram.org/bot";

    public void sendReminder(Long chatId, String medicineName, String dosage, Long reminderId) {
        String url = TELEGRAM_API_URL + botToken + "/sendMessage";

        Map<String, Object> body = new HashMap<>();
        body.put("chat_id", chatId);
        body.put("text", String.format("💊 Пора принять %s (%s)", medicineName, dosage));
        body.put("parse_mode", "HTML");

        Map<String, Object> replyMarkup = new HashMap<>();
        replyMarkup.put("inline_keyboard", new Object[]{
                new Object[]{
                        Map.of("text", "✅ Принял", "callback_data", "take_" + reminderId),
                        Map.of("text", "⏰ Отложить на 10 мин", "callback_data", "postpone_" + reminderId)
                }
        });
        body.put("reply_markup", replyMarkup);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

        restTemplate.postForEntity(url, request, String.class);
    }
}