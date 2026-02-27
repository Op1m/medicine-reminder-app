package com.op1m.medrem.backend_api.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.op1m.medrem.backend_api.dto.DTOMapper;
import com.op1m.medrem.backend_api.dto.TelegramUserDTO;
import com.op1m.medrem.backend_api.entity.User;
import com.op1m.medrem.backend_api.security.JwtTokenProvider;
import com.op1m.medrem.backend_api.security.TelegramInitDataValidator;
import com.op1m.medrem.backend_api.service.UserService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping({"/api/auth", "/"})
public class TelegramAuthController {

    private final UserService userService;
    private final JwtTokenProvider tokenProvider;
    private final PasswordEncoder passwordEncoder;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${TELEGRAM_BOT_TOKEN:${app.telegram.bot-token:}}")
    private String botToken;

    public TelegramAuthController(UserService userService, JwtTokenProvider tokenProvider, PasswordEncoder passwordEncoder) {
        this.userService = userService;
        this.tokenProvider = tokenProvider;
        this.passwordEncoder = passwordEncoder;
    }

    public static class InitDataRequest {
        public String initData;
    }

    @PostMapping(path = {"/telegram", "/auth/telegram"})
    public ResponseEntity<?> loginWithTelegram(@RequestBody InitDataRequest body) {
        System.out.println("=== /api/auth/telegram called ===");

        if (body == null || body.initData == null || body.initData.isBlank()) {
            System.out.println("❌ missing initData");
            return ResponseEntity.badRequest().body(Map.of("error", "missing initData"));
        }

        String initData = body.initData;

        System.out.println("raw body.initData: [" + initData + "]");

        boolean ok = TelegramInitDataValidator.validateInitData(initData, botToken);
        if (!ok) {
            System.out.println("❌ invalid initData");
            return ResponseEntity.status(401).body(Map.of("error", "invalid initData"));
        }

        try {
            var decodedParams = TelegramInitDataValidator.parseDecodedParams(initData);
            String userJson = decodedParams.get("user");
            if (userJson == null || userJson.isBlank()) {
                System.out.println("❌ user json not present in initData");
                return ResponseEntity.badRequest().body(Map.of("error", "cannot parse user data"));
            }

            JsonNode userNode = objectMapper.readTree(userJson);

            long tgId = userNode.has("id") ? userNode.get("id").asLong() : 0L;
            String firstName = userNode.has("first_name") ? userNode.get("first_name").asText("") : "";
            String lastName = userNode.has("last_name") ? userNode.get("last_name").asText("") : "";
            String username = userNode.has("username") ? userNode.get("username").asText("") : "";
            String photoUrl = userNode.has("photo_url") ? userNode.get("photo_url").asText("") : "";

            if (tgId == 0L) {
                System.out.println("❌ tgId parsed as 0");
                return ResponseEntity.badRequest().body(Map.of("error", "cannot parse user id"));
            }

            User user = userService.findByTelegramId(tgId);
            if (user == null) {
                user = new User();
                user.setTelegramId(tgId);
                user.setUsername((username != null && !username.isBlank()) ? username : "tg_" + tgId);
                user.setFirstName(firstName);
                user.setLastName(lastName);
                user.setPhotoUrl(photoUrl);
                user.setEmail("telegram_" + tgId + "@placeholder.local");
                String randomPassword = UUID.randomUUID().toString();
                user.setPassword(passwordEncoder.encode(randomPassword));
                user = userService.save(user);
                System.out.println("Created new user id=" + (user.getId() != null ? user.getId() : "n/a") + " tg=" + tgId);
            } else {
                user.setFirstName(firstName);
                user.setLastName(lastName);
                if (username != null && !username.isBlank()) user.setUsername(username);
                user.setPhotoUrl(photoUrl);
                user = userService.update(user);
                System.out.println("Updated user id=" + user.getId() + " tg=" + tgId);
            }

            String token = tokenProvider.generateToken(user);

            return ResponseEntity.ok(Map.of(
                    "token", token,
                    "user", DTOMapper.toUserDTO(user)
            ));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of("error", "internal_error"));
        }
    }

    @PostMapping("/telegram/debug")
    public ResponseEntity<?> debugLogin(@RequestBody TelegramUserDTO dto) {
        User user = userService.findByTelegramId(dto.getId());
        if (user == null) {
            user = new User();
            user.setTelegramId(dto.getId());
            user.setUsername(dto.getUsername() == null ? "tg_" + dto.getId() : dto.getUsername());
            user.setFirstName(dto.getFirstName());
            user.setLastName(dto.getLastName());
            user.setPhotoUrl(dto.getPhotoUrl());
            user.setEmail("telegram_" + dto.getId() + "@placeholder.local");
            String randomPassword = UUID.randomUUID().toString();
            user.setPassword(passwordEncoder.encode(randomPassword));
            user = userService.save(user);
        } else {
            user.setFirstName(dto.getFirstName());
            user.setLastName(dto.getLastName());
            if (dto.getUsername() != null) user.setUsername(dto.getUsername());
            user.setPhotoUrl(dto.getPhotoUrl());
            user = userService.update(user);
        }
        String token = tokenProvider.generateToken(user);
        return ResponseEntity.ok(Map.of("token", token, "user", DTOMapper.toUserDTO(user)));
    }
}
