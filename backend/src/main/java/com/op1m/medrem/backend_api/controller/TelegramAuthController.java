package com.op1m.medrem.backend_api.controller;

import com.op1m.medrem.backend_api.dto.DTOMapper;
import com.op1m.medrem.backend_api.dto.UserDTO;
import com.op1m.medrem.backend_api.entity.User;
import com.op1m.medrem.backend_api.security.JwtTokenProvider;
import com.op1m.medrem.backend_api.security.TelegramInitDataValidator;
import com.op1m.medrem.backend_api.security.TelegramInitDataValidator.DebugInfo;
import com.op1m.medrem.backend_api.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping({"/auth/telegram", "/api/auth/telegram"})
public class TelegramAuthController {

    private static final Logger log = LoggerFactory.getLogger(TelegramAuthController.class);
    private static final ObjectMapper OM = new ObjectMapper();

    @Value("${telegram.bot.token:${TELEGRAM_BOT_TOKEN:}}")
    private String botToken;

    @Autowired
    private UserService userService;

    @Autowired
    private JwtTokenProvider tokenProvider;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> fromWidget(@RequestBody Map<String, Object> body) {
        Map<String, String> flat = body.entrySet().stream()
                .filter(e -> e.getValue() != null)
                .collect(Collectors.toMap(Map.Entry::getKey, e -> String.valueOf(e.getValue())));
        return handle(flat);
    }

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> fromRedirect(@RequestParam Map<String, String> params) {
        return handle(params);
    }

    private ResponseEntity<?> handle(Map<String, String> data) {

        if (botToken == null || botToken.isBlank()) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("ok", false, "error", "server missing TELEGRAM_BOT_TOKEN"));
        }

        DebugInfo dbg = TelegramInitDataValidator.validateWithDebug(data, botToken);

        if (!dbg.ok) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("ok", false, "error", "invalid init data"));
        }

        Map<String, String> unpacked = new HashMap<>();

        if (data.containsKey("initData")) {
            String raw = data.get("initData");
            if (raw != null) {
                String[] pairs = raw.split("&");
                for (String p : pairs) {
                    int idx = p.indexOf('=');
                    if (idx <= 0) continue;
                    String k = java.net.URLDecoder.decode(p.substring(0, idx), java.nio.charset.StandardCharsets.UTF_8);
                    String v = java.net.URLDecoder.decode(p.substring(idx + 1), java.nio.charset.StandardCharsets.UTF_8);
                    unpacked.put(k, v);
                }
            }
        } else {
            unpacked.putAll(data);
        }

        Map<String, Object> userMap = extractUser(unpacked);
        if (userMap.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("ok", false, "error", "user not found in initData"));
        }

        Long tgId = Long.parseLong((String) userMap.get("id"));
        String firstName = (String) userMap.get("first_name");
        String lastName = (String) userMap.get("last_name");
        String username = (String) userMap.get("username");
        String photoUrl = (String) userMap.get("photo_url");

        User user = userService.findByTelegramId(tgId);
        if (user == null) {
            user = new User();
            user.setTelegramId(tgId);
            user.setUsername(username != null ? username : "tg_" + tgId);
            user.setFirstName(firstName);
            user.setLastName(lastName);
            user.setPhotoUrl(photoUrl);
            user.setEmail("telegram_" + tgId + "@placeholder.local");
            user.setPassword(passwordEncoder.encode(UUID.randomUUID().toString()));
            user.setCreatedAt(LocalDateTime.now());
            user.setUpdatedAt(LocalDateTime.now());
            user.setActive(true);
            user = userService.save(user);
        } else {
            user.setFirstName(firstName);
            user.setLastName(lastName);
            if (username != null && !username.isBlank()) {
                user.setUsername(username);
            }
            user.setPhotoUrl(photoUrl);
            user.setUpdatedAt(LocalDateTime.now());
            user = userService.update(user);
        }

        String token = tokenProvider.generateToken(user);

        UserDTO userDTO = DTOMapper.toUserDTO(user);

        return ResponseEntity.ok(Map.of(
                "ok", true,
                "token", token,
                "user", userDTO
        ));
    }

    private Map<String, Object> extractUser(Map<String, String> d) {
        Map<String, Object> u = new LinkedHashMap<>();
        if (d.containsKey("user")) {
            String userRaw = d.get("user");
            if (userRaw != null && (userRaw.startsWith("{") || userRaw.startsWith("["))) {
                try {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> parsed = OM.readValue(userRaw, Map.class);
                    if (parsed != null) {
                        copyIfPresent(parsed, "id", u);
                        copyIfPresent(parsed, "first_name", u);
                        copyIfPresent(parsed, "last_name", u);
                        copyIfPresent(parsed, "username", u);
                        copyIfPresent(parsed, "photo_url", u);
                        copyIfPresent(parsed, "auth_date", u);
                        return u;
                    }
                } catch (Exception ex) {
                    log.debug("failed to parse 'user' JSON string", ex);
                }
            }
        }

        copyIfPresent(d, "id", u);
        copyIfPresent(d, "first_name", u);
        copyIfPresent(d, "last_name", u);
        copyIfPresent(d, "username", u);
        copyIfPresent(d, "photo_url", u);
        copyIfPresent(d, "auth_date", u);

        return u;
    }

    private void copyIfPresent(Map<?, ?> src, String key, Map<String, Object> dest) {
        if (src.containsKey(key) && src.get(key) != null) dest.put(key, String.valueOf(src.get(key)));
    }

    private static String truncate(String s, int max) {
        if (s == null) return null;
        if (s.length() <= max) return s;
        return s.substring(0, max) + "...(truncated)";
    }

    private static Map<String, String> shortenedMap(Map<String, String> m, int maxValLen) {
        if (m == null) return Collections.emptyMap();
        Map<String, String> out = new LinkedHashMap<>();
        for (Map.Entry<String, String> e : m.entrySet()) {
            String v = e.getValue();
            if (v == null) v = "null";
            if (v.length() > maxValLen) v = v.substring(0, maxValLen) + "...(truncated)";
            out.put(e.getKey(), v);
        }
        return out;
    }
}