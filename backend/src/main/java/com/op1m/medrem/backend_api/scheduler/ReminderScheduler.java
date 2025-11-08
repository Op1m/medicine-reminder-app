package com.op1m.medrem.backend_api.scheduler;

import com.op1m.medrem.backend_api.entity.MedicineHistory;
import com.op1m.medrem.backend_api.entity.Reminder;
import com.op1m.medrem.backend_api.service.MedicineHistoryService;
import com.op1m.medrem.backend_api.service.ReminderService;
import jakarta.persistence.Column;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.datasource.init.CannotReadScriptException;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Component
public class ReminderScheduler {

    @Autowired
    private ReminderService reminderService;

    @Autowired
    private MedicineHistoryService medicineHistoryService;

    @Scheduled(cron = "0 * * * * *")
    @Transactional
    public void checkDueReminders() {
        System.out.println("🔔 ReminderScheduler: Проверка напоминаний...");

        List<Reminder> dueReminders = reminderService.getDueReminders();

        for(Reminder reminder : dueReminders) {
            System.out.println("⏰ Время принять: " +
                    reminder.getMedicine().getName() +
                    " (" + reminder.getMedicine().getDosage() + ")" +
                    " - Пользователь: " + reminder.getUser().getUsername());

            createHistoryRecord(reminder);
        }

        System.out.println("📊 ReminderScheduler: Найдено напоминаний: " + dueReminders.size());

        medicineHistoryService.checkAndMarkMissedDoses();
    }

    private void createHistoryRecord(Reminder reminder) {
        try {
            MedicineHistory history = medicineHistoryService.createScheduleDose(reminder.getId(), LocalDateTime.now());
            System.out.println("✅ Создана запись истории: " + history.getId());
        } catch (Exception e) {
            System.out.println("❌ Ошибка создания истории: " + e.getMessage());
        }
    }
}
