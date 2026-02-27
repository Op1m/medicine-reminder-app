package com.op1m.medrem.backend_api.controller;

import com.op1m.medrem.backend_api.dto.TelegramUserDTO;
import com.op1m.medrem.backend_api.dto.DTOMapper;
import com.op1m.medrem.backend_api.entity.User;
import com.op1m.medrem.backend_api.security.JwtTokenProvider;
import com.op1m.medrem.backend_api.security.TelegramInitDataValidator;
import com.op1m.medrem.backend_api.service.UserService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping({"/api/auth", "/"})
public class TelegramAuthController {

    private final UserService userService;
    private final JwtTokenProvider tokenProvider;

    @Value("${TELEGRAM_BOT_TOKEN:${app.telegram.bot-token:}}")
    private String botToken;

    public TelegramAuthController(UserService userService, JwtTokenProvider tokenProvider) {
        this.userService = userService;
        this.tokenProvider = tokenProvider;
    }

    public static class InitDataRequest {
        public String initData;
    }

    @PostMapping(path = {"/api/auth/telegram", "/auth/telegram"})
    public ResponseEntity<?> loginWithTelegram(@RequestBody InitDataRequest body) {
        System.out.println("=== /api/auth/telegram called ===");
        if (body == null || body.initData == null || body.initData.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "missing initData"));
        }
        
        // boolean isValid = TelegramInitDataValidator.validateInitData(body.initData, botToken);
        // if (!isValid) {
        //     System.out.println("❌ Validation failed for initData: " + body.initData);
        //     return ResponseEntity.status(401).body(Map.of("error", "invalid initData"));
        // }

        Map<String, String> decodedParams = TelegramInitDataValidator.parseDecodedParams(body.initData);

        TelegramInitDataValidator.TelegramUserData userData = TelegramInitDataValidator.extractUser(decodedParams);
        if (userData == null) {
            System.out.println("❌ Cannot parse user data from decoded params");
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