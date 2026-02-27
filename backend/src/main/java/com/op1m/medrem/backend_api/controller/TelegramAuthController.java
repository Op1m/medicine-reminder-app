package com.op1m.medrem.backend_api.controller;

import com.op1m.medrem.backend_api.security.TelegramInitDataValidator;
import com.op1m.medrem.backend_api.security.TelegramInitDataValidator.DebugInfo;
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
@RequestMapping("/auth/telegram")
public class TelegramAuthController {

    private static final Logger log = LoggerFactory.getLogger(TelegramAuthController.class);

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

        log.debug("Incoming telegram auth data (keys and truncated values): {}", shortenedMap(data, 120));

        DebugInfo dbg = TelegramInitDataValidator.validateWithDebug(data, botToken);

        if (!dbg.ok) {
            log.debug("Telegram init data validation failed. receivedKeys={}, note={}", dbg.receivedKeys, dbg.note);
            log.debug("Validation debug summary: providedHash='{}', calcHex='{}', secretKeySha256='{}'",
                    truncate(dbg.providedHash, 80), truncate(dbg.calcHex, 80), truncate(dbg.secretKeyHex, 80));
            log.debug("data_check_string (first 2000 chars):\n{}", truncate(dbg.dataCheckString, 2000));
            log.debug("parts (first 30): {}", dbg.parts == null ? "null" : dbg.parts.size() > 30 ? dbg.parts.subList(0,30) : dbg.parts);
            if (dbg.error != null) log.debug("validation error reason: {}", dbg.error);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("ok", false, "error", "invalid init data"));
        }

        Map<String, String> user = extractUser(data);
        log.info("Telegram login OK for id={} username={}", user.get("id"), user.get("username"));

        return ResponseEntity.ok(Map.of("ok", true, "user", user));
    }

    private Map<String, String> extractUser(Map<String, String> d) {
        Map<String, String> u = new HashMap<>();
        for (String k : List.of("id", "first_name", "last_name", "username", "photo_url", "auth_date")) {
            if (d.containsKey(k)) u.put(k, d.get(k));
        }
        return u;
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
