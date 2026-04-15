package com.op1m.medrem.backend_api.scheduler;

import com.op1m.medrem.backend_api.entity.MedicineHistory;
import com.op1m.medrem.backend_api.entity.Reminder;
import com.op1m.medrem.backend_api.service.MedicineHistoryService;
import com.op1m.medrem.backend_api.service.NotificationService;
import com.op1m.medrem.backend_api.service.ReminderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;

@Component
public class ReminderScheduler {

    @Autowired
    private ReminderService reminderService;

    @Autowired
    private MedicineHistoryService medicineHistoryService;

    @Autowired
    private NotificationService notificationService;

    @Scheduled(cron = "0 * * * * *")
    @Transactional
    public void checkDueReminders() {
        System.out.println("Проверка напоминаний... " + OffsetDateTime.now(ZoneOffset.UTC));

        List<Reminder> dueReminders = reminderService.getDueReminders();

        for (Reminder reminder : dueReminders) {
            System.out.println("Время принять: " +
                    reminder.getMedicine().getName() +
                    " (" + reminder.getMedicine().getDosage() + ")" +
                    " - Пользователь: " + reminder.getUser().getUsername() +
                    " - Время: " + reminder.getReminderTime());

            MedicineHistory history = createHistoryRecord(reminder);

            if (history != null) {
                notificationService.notifyUser(reminder);
            }
        }

        System.out.println("Найдено напоминаний: " + dueReminders.size());

        medicineHistoryService.checkPostponedReminders();

        medicineHistoryService.checkAndMarkMissedDoses();
    }

    private MedicineHistory createHistoryRecord(Reminder reminder) {
        try {
            MedicineHistory history = medicineHistoryService.createScheduleDose(
                reminder.getId(),
                OffsetDateTime.now(ZoneOffset.UTC)
            );
            System.out.println("Создана запись истории: " + history.getId() +
                ", время: " + OffsetDateTime.now(ZoneOffset.UTC));
            return history;
        } catch (Exception e) {
            System.out.println("Ошибка создания истории: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
}