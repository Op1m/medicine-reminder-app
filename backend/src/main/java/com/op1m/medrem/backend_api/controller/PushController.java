package com.op1m.medrem.backend_api.controller;

import com.op1m.medrem.backend_api.entity.PushSubscription;
import com.op1m.medrem.backend_api.entity.User;
import com.op1m.medrem.backend_api.repository.PushSubscriptionRepository;
import com.op1m.medrem.backend_api.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.Map;

@RestController
@RequestMapping("/api/push")
public class PushController {

    @Autowired
    private PushSubscriptionRepository subscriptionRepository;

    @Autowired
    private UserRepository userRepository;

    @Value("${vapid.public.key}")
    private String vapidPublicKey;

    @GetMapping("/public-key")
    public ResponseEntity<String> getPublicKey() {
        return ResponseEntity.ok(vapidPublicKey);
    }

    @PostMapping("/subscribe")
    public ResponseEntity<?> subscribe(@RequestBody Map<String, Object> subscriptionData, Principal principal) {
        String username = principal.getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        String endpoint = (String) subscriptionData.get("endpoint");
        Map<String, String> keys = (Map<String, String>) subscriptionData.get("keys");
        String p256dh = keys.get("p256dh");
        String auth = keys.get("auth");

        subscriptionRepository.findByUserAndEndpoint(user, endpoint)
                .ifPresentOrElse(
                        sub -> {
                            sub.setP256dh(p256dh);
                            sub.setAuth(auth);
                            subscriptionRepository.save(sub);
                        },
                        () -> {
                            PushSubscription sub = new PushSubscription();
                            sub.setUser(user);
                            sub.setEndpoint(endpoint);
                            sub.setP256dh(p256dh);
                            sub.setAuth(auth);
                            subscriptionRepository.save(sub);
                        }
                );

        return ResponseEntity.ok().build();
    }
}