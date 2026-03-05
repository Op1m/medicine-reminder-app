package com.op1m.medrem.backend_api.service;

import com.op1m.medrem.backend_api.entity.PushSubscription;
import nl.martijndwars.webpush.Notification;
import nl.martijndwars.webpush.PushService;
import nl.martijndwars.webpush.Subscription;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import com.op1m.medrem.backend_api.repository.PushSubscriptionRepository;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.Security;
import java.util.concurrent.ExecutionException;
import org.jose4j.lang.JoseException;

@Service
public class WebPushService {

    @Value("${vapid.public.key}")
    private String vapidPublicKey;

    @Value("${vapid.private.key}")
    private String vapidPrivateKey;

    @Value("${vapid.subject}")
    private String vapidSubject;

    @Autowired
    private PushSubscriptionRepository pushSubscriptionRepository;

    private PushService pushService;

    @PostConstruct
    public void init() throws GeneralSecurityException {
        Security.addProvider(new BouncyCastleProvider());
        pushService = new PushService(vapidPublicKey, vapidPrivateKey, vapidSubject);
    }

    public void sendNotification(PushSubscription subscription, String title, String body, Long reminderId) {
        try {
            Subscription sub = new Subscription(
                    subscription.getEndpoint(),
                    new Subscription.Keys(subscription.getP256dh(), subscription.getAuth())
            );

            String payload = String.format(
                    "{\"title\":\"%s\",\"body\":\"%s\",\"reminderId\":%d}",
                    title, body, reminderId
            );

            Notification notification = new Notification(sub, payload);
            pushService.send(notification);
            System.out.println("✅ Push-уведомление отправлено: " + reminderId);

        } catch (GeneralSecurityException | ExecutionException | InterruptedException | IOException | JoseException e) {
            System.err.println("❌ Ошибка отправки push-уведомления: " + e.getMessage());
            e.printStackTrace();

            if (e.getMessage() != null && (e.getMessage().contains("410") || e.getMessage().contains("Gone"))) {
                System.out.println("⚠️ Подписка устарела, удаляем: " + subscription.getId());
                try {
                    pushSubscriptionRepository.delete(subscription);
                } catch (Exception ex) {
                    System.err.println("Ошибка при удалении подписки: " + ex.getMessage());
                }
            }
        }
    }
}