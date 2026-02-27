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

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping({"/api/auth", "/"})
public class TelegramAuthController {

    private final UserService userService;
    private final JwtTokenProvider tokenProvider;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.telegram.bot-token:}")
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
        try {
            System.out.println("=== /api/auth/telegram called ===");
            if (body == null || body.initData == null || body.initData.isBlank()) {
                return ResponseEntity.badRequest().body(Map.of("error", "missing initData"));
            }

            boolean ok = TelegramInitDataValidator.validateInitData(body.initData, botToken);
            if (!ok) {
                System.out.println("❌ invalid initData");
                return ResponseEntity.status(401).body(Map.of("error", "invalid initData"));
            }

            var decodedParams = TelegramInitDataValidator.parseDecodedParams(body.initData);
            var userData = TelegramInitDataValidator.extractUser(decodedParams);
            if (userData == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "cannot parse user data"));
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
                user = userService.save(user);
            } else {
                user.setFirstName(firstName);
                user.setLastName(lastName);
                if (username != null && !username.isBlank()) user.setUsername(username);
                user.setPhotoUrl(photoUrl);
                user = userService.update(user);
            }

            String token = tokenProvider.generateToken(user);

            return ResponseEntity.ok(Map.of(
                    "token", token,
                    "user", DTOMapper.toUserDTO(user)
            ));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body(Map.of("error", "internal"));
        }
    }

    @PostMapping("/telegram/debug")
    public ResponseEntity<?> debugLogin(@RequestBody TelegramUserDTO dto) {
        try {
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
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body(Map.of("error", "internal"));
        }
    }
}
