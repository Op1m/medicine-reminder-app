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
import java.util.HashMap;

@RestController
@RequestMapping("/auth")
public class TelegramAuthController {

    private final UserService userService;
    private final JwtTokenProvider tokenProvider;

    @Value("${app.telegram.bot-token}")
    private String botToken;

    public TelegramAuthController(UserService userService, JwtTokenProvider tokenProvider) {
        this.userService = userService;
        this.tokenProvider = tokenProvider;
    }

    public static class InitDataRequest { public String initData; }

    @PostMapping("/telegram")
    public ResponseEntity<?> loginWithTelegram(@RequestBody InitDataRequest body) {
        if (body == null || body.initData == null) {
            return ResponseEntity.badRequest().body(Map.of("error","missing initData"));
        }

        boolean valid = TelegramInitDataValidator.validateInitData(body.initData, botToken);
        if (!valid) {
            return ResponseEntity.status(401).body(Map.of("error","invalid initData"));
        }

        Map<String,String> map = TelegramInitDataValidator.parseInitData(body.initData);
        Long tgId = Long.parseLong(map.get("user[id]").replaceAll("[^0-9]",""));
        String firstName = map.getOrDefault("user[first_name]", map.get("first_name"));
        String lastName = map.getOrDefault("user[last_name]", map.get("last_name"));
        String username = map.getOrDefault("user[username]", map.get("username"));
        String photoUrl = map.getOrDefault("user[photo_url]", map.get("photo_url"));

        User user = userService.findByTelegramId(tgId);
        if (user == null) {
            String generatedUsername = (username != null && !username.isBlank()) ? username : ("tg_" + tgId);
            user = new User();
            user.setUsername(generatedUsername);
            user.setFirstName(firstName);
            user.setLastName(lastName);
            user.setTelegramId(tgId);
            user.setPhotoUrl(photoUrl);
            user.setActive(true);
            user = userService.save(user);
        } else {
            user.setFirstName(firstName);
            user.setLastName(lastName);
            if (username != null && !username.isBlank()) user.setUsername(username);
            user.setPhotoUrl(photoUrl);
            user = userService.update(user);
        }

        String token = tokenProvider.generateToken(user);

        Map<String,Object> resp = new HashMap<>();
        resp.put("token", token);
        resp.put("user", DTOMapper.toUserDTO(user));

        return ResponseEntity.ok(resp);
    }
}