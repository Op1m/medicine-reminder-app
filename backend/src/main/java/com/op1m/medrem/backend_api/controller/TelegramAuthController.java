package com.op1m.medrem.backend_api.controller;

import com.op1m.medrem.backend_api.security.TelegramInitDataValidator;
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

        try {
            boolean ok = TelegramInitDataValidator.validate(data, botToken);
            if (!ok) {
                log.debug("Telegram init data validation failed. data_check_keys={}", data.keySet());
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("ok", false, "error", "invalid init data"));
            }

            Map<String, String> user = extractUser(data);

            log.info("Telegram login OK for id={} username={}", user.get("id"), user.get("username"));
            return ResponseEntity.ok(Map.of("ok", true, "user", user));
        } catch (Exception ex) {
            log.error("Error validating telegram init data", ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("ok", false, "error", ex.getMessage()));
        }
    }

    private Map<String, String> extractUser(Map<String, String> d) {
        Map<String, String> u = new HashMap<>();
        for (String k : List.of("id", "first_name", "last_name", "username", "photo_url", "auth_date")) {
            if (d.containsKey(k)) u.put(k, d.get(k));
        }
        return u;
    }
}
