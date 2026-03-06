package com.op1m.medrem.backend_api.controller;

import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import java.util.Map;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@RestController
@RequestMapping("/api/logs")
public class LogController {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @PostMapping("/frontend")
    public ResponseEntity<?> receiveFrontendLog(@RequestBody Map<String, Object> logData) {
        String timestamp = LocalDateTime.now().format(FORMATTER);
        String level = (String) logData.getOrDefault("level", "INFO");
        String message = (String) logData.getOrDefault("message", "");
        Object data = logData.get("data");
        String userId = (String) logData.getOrDefault("userId", "unknown");
        String url = (String) logData.getOrDefault("url", "");

        System.out.println("\n📱═══════════════════════════════════════════");
        System.out.println("📱 FRONTEND LOG [" + timestamp + "] " + level);
        System.out.println("📱 UserID: " + userId);
        System.out.println("📱 URL: " + url);
        System.out.println("📱 Message: " + message);
        if (data != null) {
            System.out.println("📱 Data: " + data);
        }
        System.out.println("📱═══════════════════════════════════════════\n");


        return ResponseEntity.ok().build();
    }

    @GetMapping("/last")
    public ResponseEntity<String> getLastLogs() {
        return ResponseEntity.ok("Логирование работает");
    }
}