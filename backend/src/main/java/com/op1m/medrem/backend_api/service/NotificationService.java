package com.op1m.medrem.backend_api.service;

import com.op1m.medrem.backend_api.entity.PushSubscription;
import com.op1m.medrem.backend_api.entity.Reminder;
import com.op1m.medrem.backend_api.entity.User;
import com.op1m.medrem.backend_api.repository.PushSubscriptionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class NotificationService {

    @Autowired
    private TelegramBotService telegramBotService;

    @Autowired
    private WebPushService webPushService;

    @Autowired
    private PushSubscriptionRepository pushSubscriptionRepository;

    public void notifyUser(Reminder reminder) {
        User user = reminder.getUser();
        Long chatId = user.getTelegramChatId() != null ? user.getTelegramChatId() : user.getTelegramId();

        String medicineName = reminder.getMedicine().getName();
        String dosage = reminder.getMedicine().getDosage();
        Long reminderId = reminder.getId();

        if (chatId != null) {
            try {
                telegramBotService.sendReminder(chatId, medicineName, dosage, reminderId);
                System.out.println("✅ Telegram-уведомление отправлено пользователю: " + user.getUsername());
            } catch (Exception e) {
                System.err.println("❌ Ошибка отправки Telegram: " + e.getMessage());
            }
        }

        List<PushSubscription> subs = pushSubscriptionRepository.findByUser(user);
        for (PushSubscription sub : subs) {
            webPushService.sendNotification(
                    sub,
                    "💊 Пора принять лекарство",
                    medicineName + " (" + dosage + ")",
                    reminderId
            );
        }
    }
}