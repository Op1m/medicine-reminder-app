package com.op1m.medrem.backend_api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
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

    @PostMapping(path = {"/telegram", "/auth/telegram", "/api/auth/telegram"})
    public ResponseEntity<?> loginWithTelegram(@RequestBody InitDataRequest body) {
        if (body == null || body.initData == null || body.initData.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "missing initData"));
        }

        boolean ok = TelegramInitDataValidator.validateInitData(body.initData, botToken);
        if (!ok) {
            return ResponseEntity.status(401).body(Map.of("error", "invalid initData"));
        }

        try {
            var decoded = TelegramInitDataValidator.parseDecodedParams(body.initData);
            String userJson = decoded.get("user");
            if (userJson == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "no user data"));
            }

            TelegramUserDTO dto = objectMapper.readValue(userJson, TelegramUserDTO.class);

            long tgId = dto.getId();
            User user = userService.findByTelegramId(tgId);
            if (user == null) {
                user = new User();
                user.setTelegramId(tgId);
                user.setUsername(dto.getUsername() == null || dto.getUsername().isBlank() ? "tg_" + tgId : dto.getUsername());
                user.setFirstName(dto.getFirstName());
                user.setLastName(dto.getLastName());
                user.setPhotoUrl(dto.getPhotoUrl());
                user.setEmail("telegram_" + tgId + "@placeholder.local");
                String randomPassword = UUID.randomUUID().toString();
                user.setPassword(passwordEncoder.encode(randomPassword));
                user = userService.save(user);
            } else {
                user.setFirstName(dto.getFirstName());
                user.setLastName(dto.getLastName());
                if (dto.getUsername() != null && !dto.getUsername().isBlank()) user.setUsername(dto.getUsername());
                user.setPhotoUrl(dto.getPhotoUrl());
                user = userService.update(user);
            }

            String token = tokenProvider.generateToken(user);

            return ResponseEntity.ok(Map.of("token", token, "user", DTOMapper.toUserDTO(user)));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body(Map.of("error", "server_error"));
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
            return ResponseEntity.internalServerError().body(Map.of("error", "server_error"));
        }
    }
}
