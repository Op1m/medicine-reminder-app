package com.op1m.medrem.backend_api.service;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class SseEmitterManager {

    private final Map<Long, SseEmitter> emitters = new ConcurrentHashMap<>();

    public void addEmitter(Long userId, SseEmitter emitter) {
        emitters.put(userId, emitter);
        emitter.onCompletion(() -> emitters.remove(userId));
        emitter.onTimeout(() -> emitters.remove(userId));
        emitter.onError((e) -> emitters.remove(userId));
    }

    public void sendUpdate(Long userId, String event, Object data) {
        SseEmitter emitter = emitters.get(userId);
        if (emitter != null) {
            try {
                emitter.send(SseEmitter.event()
                        .name(event)
                        .data(data));
                System.out.println("📡 SSE отправлено пользователю " + userId + ": " + event);
            } catch (IOException e) {
                System.err.println("❌ Ошибка отправки SSE: " + e.getMessage());
                emitters.remove(userId);
            }
        } else {
            System.out.println("⚠️ Нет активного SSE соединения для пользователя " + userId);
        }
    }

    public void removeEmitter(Long userId) {
        emitters.remove(userId);
    }
}