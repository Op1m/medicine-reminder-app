package com.op1m.medrem.backend_api.controller;

import com.op1m.medrem.backend_api.security.TelegramInitDataValidator;
import com.op1m.medrem.backend_api.security.TelegramInitDataValidator.DebugInfo;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping({"/auth/telegram", "/api/auth/telegram"})
public class TelegramAuthController {

    private static final Logger log = LoggerFactory.getLogger(TelegramAuthController.class);
    private static final ObjectMapper OM = new ObjectMapper();

    @Value("${telegram.bot.token:${TELEGRAM_BOT_TOKEN:}}")
    private String botToken;

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
            log.error("TELEGRAM BOT TOKEN is not configured");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("ok", false, "error", "server missing TELEGRAM_BOT_TOKEN"));
        }

        log.debug("Incoming telegram auth data (keys and truncated values): {}", shortenedMap(data, 200));

        DebugInfo dbg = TelegramInitDataValidator.validateWithDebug(data, botToken);

        if (!dbg.ok) {
            log.debug("Telegram init data validation failed. receivedKeys={}, note={}", dbg.receivedKeys, dbg.note);
            log.debug("Validation debug summary: providedHash='{}', calcHex='{}', secretKey='{}'",
                    truncate(dbg.providedHash, 100), truncate(dbg.calcHex, 100), truncate(dbg.secretKeyHex, 100));
            log.debug("data_check_string (first 2000 chars):\n{}", truncate(dbg.dataCheckString, 2000));
            if (dbg.error != null) log.debug("validation error reason: {}", dbg.error);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("ok", false, "error", "invalid init data"));
        }

        Map<String, Object> user = extractUser(data);
        if (user.isEmpty()) {
            log.warn("Validated but no user info could be extracted. raw keys={}", data.keySet());
            return ResponseEntity.status(HttpStatus.OK)
                    .body(Map.of("ok", true, "user", Map.of()));
        }

        log.info("Telegram login OK for id={} username={}", user.get("id"), user.get("username"));
        Map<String, Object> result = new HashMap<>();
        result.put("ok", true);
        result.put("user", user);
        return ResponseEntity.ok(result);
    }

    private Map<String, Object> extractUser(Map<String, String> d) {
        Map<String, Object> u = new LinkedHashMap<>();
        // 1) если присутствует ключ "user" и это JSON-строка — распарсить
        if (d.containsKey("user")) {
            String userRaw = d.get("user");
            if (userRaw != null && (userRaw.startsWith("{") || userRaw.startsWith("["))) {
                try {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> parsed = OM.readValue(userRaw, Map.class);
                    if (parsed != null) {
                        // перенести только нужные поля
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

        // 2) fallback: брать плоские поля из параметров
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
