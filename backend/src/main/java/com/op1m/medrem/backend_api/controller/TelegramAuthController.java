package com.op1m.medrem.backend_api.controller;

import com.op1m.medrem.backend_api.entity.User;
import com.op1m.medrem.backend_api.security.JwtTokenProvider;
import com.op1m.medrem.backend_api.security.TelegramInitDataValidator;
import com.op1m.medrem.backend_api.service.UserService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/auth")
public class TelegramAuthController {

    private final UserService userService;
    private final JwtTokenProvider jwtTokenProvider;

    @Value("${app.telegram.bot-token}")
    private String botToken;

    public TelegramAuthController(UserService userService, JwtTokenProvider jwtTokenProvider) {
        this.userService = userService;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    public static class InitDataRequest { public String initData; }

    @PostMapping("/telegram")
    public ResponseEntity<?> loginWithTelegram(@RequestBody InitDataRequest body) {
        if (body == null || body.initData == null || body.initData.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "missing initData"));
        }

        boolean valid = TelegramInitDataValidator.validateInitData(body.initData, botToken);
        if (!valid) {
            return ResponseEntity.status(401).body(Map.of("error", "invalid initData"));
        }

        var data = TelegramInitDataValidator.parseInitData(body.initData);

        String idStr = data.getOrDefault("user[id]", data.getOrDefault("id", null));
        if (idStr == null) {
            return ResponseEntity.status(400).body(Map.of("error", "user id not found in initData"));
        }

        long tgId;
        try {
            tgId = Long.parseLong(idStr.replaceAll("[^0-9]", ""));
        } catch (Exception e) {
            return ResponseEntity.status(400).body(Map.of("error", "invalid user id"));
        }

        String firstName = data.getOrDefault("user[first_name]", data.getOrDefault("first_name", ""));
        String lastName = data.getOrDefault("user[last_name]", data.getOrDefault("last_name", ""));
        String username = data.getOrDefault("user[username]", data.getOrDefault("username", ""));
        String photoUrl = data.getOrDefault("user[photo_url]", data.getOrDefault("photo_url", ""));

        User user = userService.findByTelegramId(tgId);
        if (user == null) {
            user = new User();
            user.setTelegramId(tgId);
            user.setUsername(username != null && !username.isBlank() ? username : "tg_" + tgId);
            user.setFirstName(firstName);
            user.setLastName(lastName);
            user.setPhotoUrl(photoUrl);
            user = userService.save(user);
        } else {
            user.setFirstName(firstName);
            user.setLastName(lastName);
            if (username != null && !username.isBlank()) user.setUsername(username);
            user.setPhotoUrl(photoUrl);
            user = userService.update(user);
        }

        String token = jwtTokenProvider.generateToken(user);

        return ResponseEntity.ok(Map.of("token", token, "user", Map.of(
                "id", user.getId(),
                "telegramId", user.getTelegramId(),
                "username", user.getUsername(),
                "firstName", user.getFirstName(),
                "lastName", user.getLastName(),
                "photoUrl", user.getPhotoUrl()
        )));
    }
}
