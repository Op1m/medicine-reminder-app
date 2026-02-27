package com.op1m.medrem.backend_api.controller;

import com.op1m.medrem.backend_api.dto.TelegramUserDTO;
import com.op1m.medrem.backend_api.dto.DTOMapper;
import com.op1m.medrem.backend_api.entity.User;
import com.op1m.medrem.backend_api.security.JwtTokenProvider;
import com.op1m.medrem.backend_api.security.TelegramInitDataValidator;
import com.op1m.medrem.backend_api.service.UserService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping({"/api/auth", "/"})
public class TelegramAuthController {

    private final UserService userService;
    private final JwtTokenProvider tokenProvider;
    private final PasswordEncoder passwordEncoder;

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

    @PostMapping(path = {"/telegram", "/auth/telegram", "/api/auth/telegram"})
    public ResponseEntity<?> loginWithTelegram(@RequestBody InitDataRequest body) {
        System.out.println("=== /api/auth/telegram called ===");

        if (body == null || body.initData == null || body.initData.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "missing initData"));
        }

        boolean ok = TelegramInitDataValidator.validateInitData(body.initData, botToken);
        if (!ok) {
            System.out.println("❌ invalid initData");
            return ResponseEntity.status(401).body(Map.of("error", "invalid initData"));
        }

        var decoded = TelegramInitDataValidator.parseDecodedParams(body.initData);
        var userData = TelegramInitDataValidator.extractUser(decoded);
        if (userData == null) {
            System.out.println("❌ cannot parse user data");
            return ResponseEntity.badRequest().body(Map.of("error", "cannot parse user data"));
        }

        try {
            String ad = decoded.get("auth_date");
            if (ad == null) return ResponseEntity.status(401).body(Map.of("error", "missing auth_date"));
            long authDate = Long.parseLong(ad);
            long now = Instant.now().getEpochSecond();
            if (Math.abs(now - authDate) > 300L) {
                return ResponseEntity.status(401).body(Map.of("error", "initData expired"));
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            return ResponseEntity.status(401).body(Map.of("error", "invalid auth_date"));
        }

        long tgId = userData.id;
        String firstName = userData.firstName;
        String lastName = userData.lastName;
        String username = userData.username;
        String photoUrl = userData.photoUrl;

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

            try {
                user.setActive(true);
            } catch (NoSuchMethodError | AbstractMethodError ignore) {
            }

            user = userService.save(user);
        } else {
            boolean changed = false;
            if (firstName != null && !firstName.isBlank() && !firstName.equals(user.getFirstName())) {
                user.setFirstName(firstName);
                changed = true;
            }
            if (lastName != null && !lastName.isBlank() && !lastName.equals(user.getLastName())) {
                user.setLastName(lastName);
                changed = true;
            }
            if (username != null && !username.isBlank() && !username.equals(user.getUsername())) {
                user.setUsername(username);
                changed = true;
            }
            if (photoUrl != null && !photoUrl.isBlank() && !photoUrl.equals(user.getPhotoUrl())) {
                user.setPhotoUrl(photoUrl);
                changed = true;
            }
            if (changed) user = userService.update(user);
        }

        String token = tokenProvider.generateToken(user);
        return ResponseEntity.ok(Map.of("token", token, "user", DTOMapper.toUserDTO(user)));
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
            try {
                user.setActive(true);
            } catch (Throwable ignored) {}
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
