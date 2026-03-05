package com.op1m.medrem.backend_api.controller;

import com.op1m.medrem.backend_api.entity.User;
import com.op1m.medrem.backend_api.service.SseEmitterManager;
import com.op1m.medrem.backend_api.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequestMapping("/api/sse")
public class SseController {

    @Autowired
    private SseEmitterManager emitterManager;

    @Autowired
    private UserService userService;

    @GetMapping
    public SseEmitter subscribe() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        User user = userService.findByUsername(username);

        if (user == null) {
            throw new RuntimeException("Пользователь не найден");
        }

        System.out.println("📡 Новое SSE подключение для пользователя: " + user.getId() + " (" + user.getUsername() + ")");

        SseEmitter emitter = new SseEmitter(Long.MAX_VALUE);
        emitterManager.addEmitter(user.getId(), emitter);

        try {
            emitter.send(SseEmitter.event()
                    .name("connected")
                    .data("SSE соединение установлено"));
        } catch (Exception e) {
            System.err.println("❌ Ошибка при отправке приветствия: " + e.getMessage());
        }

        return emitter;
    }
}