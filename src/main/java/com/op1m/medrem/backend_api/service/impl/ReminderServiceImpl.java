package com.op1m.medrem.backend_api.service.impl;

import com.op1m.medrem.backend_api.entity.Reminder;
import com.op1m.medrem.backend_api.entity.User;
import com.op1m.medrem.backend_api.entity.Medicine;
import com.op1m.medrem.backend_api.repository.ReminderRepository;
import com.op1m.medrem.backend_api.service.ReminderService;
import com.op1m.medrem.backend_api.service.UserService;
import com.op1m.medrem.backend_api.service.MedicineService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class ReminderServiceImpl implements ReminderService{

    private final ReminderRepository reminderRepository;
    private final UserService userService;
    private final MedicineService medicineService;

    @Autowired
    public ReminderServiceImpl(ReminderRepository reminderRepository,
                               UserService userService,
                               MedicineService medicineService) {
        this.reminderRepository = reminderRepository;
        this.userService = userService;
        this.medicineService = medicineService;
    }

    @Override
    public Reminder createReminder(Long userId, Long medicineId, LocalTime reminderTime, String daysOfWeek) {
        System.out.println("⏰ ReminderService: Создание напоминания: user=" + userId + ", medicine=" + medicineId + ", time=" + reminderTime);

        User user = userService.findById(userId);
        if (user == null) {
            throw new RuntimeException("❌ ReminderService: Пользователь с ID " + userId + " не найден");
        }

        Medicine medicine = medicineService.findById(medicineId);
        if (medicine == null) {
            throw new RuntimeException("❌ ReminderService: Лекарство с ID " + medicineId + " не найден");
        }

        Reminder reminder = new Reminder(user, medicine, reminderTime);
        if (daysOfWeek != null) {
            reminder.setDaysOfWeek(daysOfWeek);
        }

        Reminder savedReminder = reminderRepository.save(reminder);
        System.out.println("✅ ReminderService: Напоминание создано: " + savedReminder.getId());
        return savedReminder;
    }

    @Override
    public List<Reminder> getUserReminders(Long userId) {
        System.out.println("📋 ReminderService: Получение напоминаний пользователя: " + userId);

        User user = userService.findById(userId);
        if (user == null) {
            throw new RuntimeException("❌ ReminderService: Пользователь с ID " + userId + " не найден");
        }

        List<Reminder> reminders = reminderRepository.findByUser(user);
        System.out.println("✅ ReminderService: Найдено напоминаний: " + reminders.size());
        return reminders;
    }

    @Override
    public List<Reminder> getUserActiveReminders(Long userId) {
        System.out.println("📋 ReminderService: Получение активных напоминаний пользователя: " + userId);

        User user = userService.findById(userId);
        if (user == null) {
            throw new RuntimeException("❌ ReminderService: Пользователь с ID " + userId + " не найден");
        }

        List<Reminder> reminders = reminderRepository.findByUserAndIsActiveTrue(user);
        System.out.println("✅ ReminderService: Найдено активных напоминаний: " + reminders.size());

        return reminders;
    }

    @Override
    public List<Reminder> getAllActiveReminders() {
        System.out.println("📋 ReminderService: Получение всех активных напоминаний");

        List<Reminder> reminders = reminderRepository.findByIsActiveTrue();
        System.out.println("✅ ReminderService: Найдено активных напоминаний: " + reminders.size());

        return reminders;
    }

    @Override
    public List<Reminder> getDueReminders() {
        System.out.println("🔔 ReminderService: Поиск напоминаний для отправки...");

        List<Reminder> dueReminders = new ArrayList<>();
        List<Reminder> activeReminders = reminderRepository.findByIsActiveTrue();

        for (Reminder reminder : activeReminders) {
            if(shouldNotifyNow(reminder)) {
                dueReminders.add(reminder);
                System.out.println("✅ ReminderService: Найдено для отправки: " + reminder.getId());
            }
        }

        System.out.println("📊 ReminderService: Итого для отправки: " + dueReminders.size());
        return dueReminders;
    }

    @Override
    public Reminder toggleReminder(Long reminderId, Boolean isActive) {
        System.out.println("🔘 ReminderService: Изменение статуса напоминания: " + reminderId + " -> " + isActive);

        Reminder reminder = reminderRepository.findById(reminderId).orElse(null);
        if(reminder == null) {
            System.out.println("❌ ReminderService: Напоминание не найдено: " + reminderId);
            return null;
        }

        reminder.setActive(isActive);
        Reminder updatedReminder = reminderRepository.save(reminder);
        System.out.println("✅ ReminderService: Статус напоминания обновлен: " + updatedReminder.getId() + " -> " + isActive);
        return updatedReminder;
    }

    @Override
    public Reminder updateReminderTime(Long reminderId, LocalTime newTime) {
        System.out.println("🕒 ReminderService: Обновление времени напоминания: " + reminderId + " -> " + newTime);

        Reminder reminder = reminderRepository.findById(reminderId).orElse(null);
        if (reminder == null) {
            System.out.println("❌ ReminderService: Напоминание не найдено: " + reminderId);
            return null;
        }

        reminder.setReminderTime(newTime);
        Reminder updatedReminder = reminderRepository.save(reminder);
        System.out.println("✅ ReminderService: Время напоминания обновлено: " + updatedReminder.getId());
        return updatedReminder;
    }

    @Override
    public boolean deleteReminder(Long reminderId) {
        System.out.println("🗑️ ReminderService: Удаление напоминания: " + reminderId);

        if(reminderRepository.existsById(reminderId)) {
            reminderRepository.deleteById(reminderId);
            System.out.println("✅ ReminderService: Напоминание удалено: " + reminderId);
            return true;
        }

        System.out.println("❌ ReminderService: Напоминание не найдено для удаления: " + reminderId);
        return false;
    }

    @Override
    public boolean shouldNotifyNow(Reminder reminder) {
        LocalDateTime now = LocalDateTime.now();
        LocalTime reminderTime = reminder.getReminderTime();

        boolean timeMatches = now.getHour() == reminderTime.getHour() &&
                now.getMinute() == reminderTime.getMinute();

        boolean dayMathes = checkDayOfWeek(reminder, now);

        return timeMatches && dayMathes;
    }

    private boolean checkDayOfWeek(Reminder reminder, LocalDateTime now) {
        String daysOfWeek = reminder.getDaysOfWeek();

        if(daysOfWeek == null || daysOfWeek.equals("everyday")) {
            return true;
        }

        int currentDay = now.getDayOfWeek().getValue();

        return daysOfWeek.contains(String.valueOf(currentDay));
    }
}
