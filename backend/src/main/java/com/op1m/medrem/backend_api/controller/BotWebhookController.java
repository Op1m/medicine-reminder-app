package com.op1m.medrem.backend_api.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.op1m.medrem.backend_api.entity.User;
import com.op1m.medrem.backend_api.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/bot")
public class BotWebhookController {

    @Autowired
    private UserService userService;

    @Value("${telegram.bot.token}")
    private String botToken;

    private final ObjectMapper mapper = new ObjectMapper();
    private final RestTemplate restTemplate = new RestTemplate();

    @PostMapping("/webhook")
    public ResponseEntity<String> handleWebhook(@RequestBody String update) {
        try {
            JsonNode root = mapper.readTree(update);
            System.out.println("📩 Получен update от Telegram: " + update);

            if (root.has("message")) {
                JsonNode message = root.get("message");
                JsonNode chat = message.get("chat");
                JsonNode from = message.get("from");

                Long realChatId = chat.get("id").asLong();
                Long telegramId = from.get("id").asLong();
                String username = from.has("username") ? from.get("username").asText() : null;
                String firstName = from.has("first_name") ? from.get("first_name").asText() : null;
                String lastName = from.has("last_name") ? from.get("last_name").asText() : null;

                System.out.println("✅ Реальный chat_id: " + realChatId);
                System.out.println("✅ Telegram ID: " + telegramId);
                System.out.println("✅ Username: " + username);

                User user = userService.findByTelegramId(telegramId);

                if (user == null) {
                    user = new User();
                    user.setTelegramId(telegramId);
                    user.setUsername(username != null ? username : "tg_" + telegramId);
                    user.setFirstName(firstName);
                    user.setLastName(lastName);
                    user = userService.save(user);
                    System.out.println("✅ Создан новый пользователь из вебхука");
                }

                if (!realChatId.equals(user.getTelegramChatId())) {
                    user.setTelegramChatId(realChatId);
                    userService.update(user);
                    System.out.println("✅ Обновлён chat_id для пользователя " + username);

                    sendWelcomeMessage(realChatId, firstName);
                }
            }

            if (root.has("callback_query")) {
                JsonNode callback = root.get("callback_query");
                String data = callback.get("data").asText();
                JsonNode from = callback.get("from");
                Long telegramId = from.get("id").asLong();

                System.out.println("🔄 Callback data: " + data);

                if (data.startsWith("take_")) {
                    Long reminderId = Long.parseLong(data.substring(5));
                    System.out.println("✅ Пользователь " + telegramId + " принял лекарство " + reminderId);
                } else if (data.startsWith("postpone_")) {
                    Long reminderId = Long.parseLong(data.substring(9));
                    System.out.println("⏰ Пользователь " + telegramId + " отложил лекарство " + reminderId);
                }
            }

            return ResponseEntity.ok().body("{\"ok\":true}");

        } catch (Exception e) {
            System.err.println("❌ Ошибка обработки вебхука: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.ok().body("{\"ok\":false, \"error\":\"" + e.getMessage() + "\"}");
        }
    }

    private void sendWelcomeMessage(Long chatId, String firstName) {
        String url = "https://api.telegram.org/bot" + botToken + "/sendMessage";

        Map<String, Object> body = new HashMap<>();
        body.put("chat_id", chatId);
        body.put("text", String.format(
                "👋 Привет, %s!\n\n" +
                        "✅ Теперь я могу отправлять тебе уведомления о приёме лекарств.\n" +
                        "💊 Ты будешь получать напоминания вовремя!",
                firstName != null ? firstName : "друг"
        ));

        try {
            restTemplate.postForEntity(url, body, String.class);
            System.out.println("✅ Приветственное сообщение отправлено в чат " + chatId);
        } catch (Exception e) {
            System.err.println("❌ Ошибка отправки приветствия: " + e.getMessage());
        }
    }
}