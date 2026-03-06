package com.op1m.medrem.backend_api.service.impl;

import com.op1m.medrem.backend_api.controller.MedicineHistoryController;
import com.op1m.medrem.backend_api.dto.DTOMapper;
import com.op1m.medrem.backend_api.dto.MedicineHistoryDTO;
import com.op1m.medrem.backend_api.entity.*;
import com.op1m.medrem.backend_api.entity.enums.MedicineStatus;
import com.op1m.medrem.backend_api.repository.MedicineHistoryRepository;
import com.op1m.medrem.backend_api.repository.ReminderRepository;
import com.op1m.medrem.backend_api.service.MedicineHistoryService;
import com.op1m.medrem.backend_api.service.ReminderService;
import com.op1m.medrem.backend_api.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.hibernate.query.sql.internal.ParameterRecognizerImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

import java.security.PublicKey;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class MedicineHistoryServiceImpl implements MedicineHistoryService {
    @Autowired
    private MedicineHistoryRepository historyRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private ReminderService reminderService;

    @Autowired
    private ReminderRepository reminderRepository;
    @Autowired
    private MedicineHistoryRepository medicineHistoryRepository;

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    @Transactional
    public MedicineHistory createScheduleDose(Long reminderId, LocalDateTime scheduledTime) {
        System.out.println("🔵 createScheduleDose: reminderId=" + reminderId + ", scheduledTime=" + scheduledTime);

        Reminder reminder = reminderRepository.findByIdWithUserAndMedicine(reminderId)
                .orElseThrow(() -> new RuntimeException("Reminder not found: " + reminderId));

        MedicineHistory history = new MedicineHistory();
        history.setReminder(reminder);
        history.setScheduledTime(scheduledTime != null ? scheduledTime : LocalDateTime.now());
        history.setStatus(MedicineStatus.PENDING);

        MedicineHistory saved = medicineHistoryRepository.save(history);
        System.out.println("✅ createScheduleDose: saved id=" + saved.getId() + ", status=" + saved.getStatus());

        return saved;
    }

    @Override
    @Transactional
    public MedicineHistory markAsTaken(Long historyId, String notes) {
        System.out.println("🟢 markAsTaken: historyId=" + historyId);

        MedicineHistory history = medicineHistoryRepository.findById(historyId)
                .orElseThrow(() -> new RuntimeException("History not found: " + historyId));

        history.setStatus(MedicineStatus.TAKEN);
        history.setTakenAt(LocalDateTime.now());

        if (notes != null) {
            history.setNotes(notes);
        }

        MedicineHistory saved = medicineHistoryRepository.save(history);
        System.out.println("✅ markAsTaken: saved id=" + saved.getId() + ", status=" + saved.getStatus());

        return saved;
    }

    @Transactional
    @Override
    public MedicineHistory markAsSkipped(Long historyId) {
        System.out.println("🟡 markAsSkipped: historyId=" + historyId);

        MedicineHistory history = medicineHistoryRepository.findWithReminderAndRelationsById(historyId);
        if (history == null) {
            throw new RuntimeException("MedicineHistory not found: " + historyId);
        }

        history.setStatus(MedicineStatus.SKIPPED);
        MedicineHistory saved = medicineHistoryRepository.save(history);

        System.out.println("✅ markAsSkipped: saved id=" + saved.getId() + ", status=" + saved.getStatus());

        return saved;
    }

    @Override
    public List<MedicineHistory> getUserMedicineHistory(Long userId) {
        User user = userService.findById(userId);
        if(user == null) {
            throw new RuntimeException("User not found");
        }
        return historyRepository.findByReminderUserOrderByScheduledTimeDesc(user);
    }

    @Override
    public List<MedicineHistory> getMedicineHistoryByStatus(Long userId, MedicineStatus status) {
        User user = userService.findById(userId);
        if(user == null) {
            throw new RuntimeException("User not found");
        }
        return historyRepository.findByReminderUserAndStatusOrderByScheduledTimeDesc(user, status);
    }

    @Override
    @Transactional(readOnly = true)
    public List<MedicineHistory> getHistoryByPeriod(Long userId, LocalDateTime start, LocalDateTime end) {
        System.out.println("📋 getHistoryByPeriod: userId=" + userId +
                ", start=" + start.format(FORMATTER) +
                ", end=" + end.format(FORMATTER));

        User user = userService.findById(userId);
        List<MedicineHistory> list = historyRepository.findByUserAndPeriodWithFetch(user, start, end);

        System.out.println("📊 Найдено записей: " + list.size());
        for (MedicineHistory h : list) {
            System.out.println("   - id=" + h.getId() +
                    ", status=" + h.getStatus() +
                    ", scheduled=" + h.getScheduledTime().format(FORMATTER) +
                    ", reminderId=" + h.getReminder().getId() +
                    ", medicine=" + h.getReminder().getMedicine().getName() +
                    ", user=" + h.getReminder().getUser().getUsername());
        }

        return list;
    }

    @Override
    @Transactional
    public void checkAndMarkMissedDoses() {
        LocalDateTime threshold = LocalDateTime.now().minusMinutes(10);
        System.out.println("🔍 checkAndMarkMissedDoses: threshold=" + threshold.format(FORMATTER));

        List<MedicineHistory> pendingHistories = historyRepository.findByStatusAndScheduledTimeBefore(
                MedicineStatus.PENDING, threshold
        );

        System.out.println("📊 Найдено просроченных PENDING: " + pendingHistories.size());

        for (MedicineHistory history : pendingHistories) {
            System.out.println("   → Отмечаем как MISSED: id=" + history.getId() +
                    ", scheduled=" + history.getScheduledTime().format(FORMATTER));
            history.markAsMissed();
        }

        if (!pendingHistories.isEmpty()) {
            historyRepository.saveAll(pendingHistories);
            System.out.println("✅ Отмечено как MISSED: " + pendingHistories.size());
        }
    }

    @Override
    @Transactional
    public MedicineHistory postponeReminder(Long reminderId, Long telegramId, int minutes) {
        System.out.println("⏰ postponeReminder: reminderId=" + reminderId +
                ", telegramId=" + telegramId +
                ", minutes=" + minutes);
        
        Reminder reminder = reminderRepository.findByIdWithUserAndMedicine(reminderId)
                .orElseThrow(() -> new RuntimeException("Reminder not found: " + reminderId));

        System.out.println("   → Найден reminder: id=" + reminder.getId() +
                ", medicine=" + reminder.getMedicine().getName() +
                ", user=" + reminder.getUser().getUsername() +
                ", user.telegramId=" + reminder.getUser().getTelegramId());

        if (!reminder.getUser().getTelegramId().equals(telegramId)) {
            System.err.println("❌ Ошибка: Telegram ID не совпадает! expected=" +
                    reminder.getUser().getTelegramId() + ", actual=" + telegramId);
            throw new RuntimeException("Telegram ID does not match reminder owner");
        }

        LocalDateTime newScheduledTime = LocalDateTime.now().plusMinutes(minutes);
        System.out.println("   → Новое запланированное время: " + newScheduledTime);

        MedicineHistory postponedHistory = new MedicineHistory(reminder, newScheduledTime);
        postponedHistory.setStatus(MedicineStatus.POSTPONED);
        postponedHistory.setNotes("Отложено на " + minutes + " минут");

        MedicineHistory saved = historyRepository.save(postponedHistory);

        System.out.println("✅ POSTPONED сохранён: id=" + saved.getId() +
                ", status=" + saved.getStatus() +
                ", scheduledTime=" + saved.getScheduledTime());

        return saved;
    }

    @Override
    @Transactional
    public void markReminderAsSkippedByBot(Long reminderId, Long telegramId) {
        System.out.println("🟡 markReminderAsSkippedByBot: reminderId=" + reminderId + ", telegramId=" + telegramId);

        Reminder reminder = reminderRepository.findById(reminderId)
                .orElseThrow(() -> new RuntimeException("Reminder not found: " + reminderId));

        if (!reminder.getUser().getTelegramId().equals(telegramId)) {
            System.err.println("❌ Ошибка: Telegram ID не совпадает! expected=" +
                    reminder.getUser().getTelegramId() + ", actual=" + telegramId);
            throw new RuntimeException("Telegram ID does not match reminder owner");
        }

        LocalDate today = LocalDate.now();
        LocalDateTime startOfDay = today.atStartOfDay();
        LocalDateTime endOfDay = today.plusDays(1).atStartOfDay().minusNanos(1);

        List<MedicineHistory> histories = historyRepository.findByUserAndPeriodWithFetch(
                reminder.getUser(), startOfDay, endOfDay);

        System.out.println("   → Найдено записей за сегодня: " + histories.size());

        MedicineHistory history = histories.stream()
                .filter(h -> h.getReminder().getId().equals(reminderId))
                .findFirst()
                .orElse(null);

        if (history == null) {
            LocalDateTime scheduledTime = today.atTime(reminder.getReminderTime());
            history = new MedicineHistory(reminder, scheduledTime);
            history = historyRepository.save(history);
            System.out.println("   → Создана новая запись: id=" + history.getId());
        }

        history.setStatus(MedicineStatus.SKIPPED);
        historyRepository.save(history);

        System.out.println("✅ Напоминание " + reminderId + " отмечено как пропущено через бота");
    }

    @Override
    @Transactional
    public void checkPostponedReminders() {
        LocalDateTime now = LocalDateTime.now();
        System.out.println("⏰ checkPostponedReminders: now=" + now.format(FORMATTER));

        List<MedicineHistory> postponedHistories = historyRepository.findByStatusAndScheduledTimeBetween(
                MedicineStatus.POSTPONED,
                now.minusMinutes(1),
                now
        );

        System.out.println("📊 Найдено POSTPONED записей для активации: " + postponedHistories.size());

        for (MedicineHistory history : postponedHistories) {
            System.out.println("   → Активируем: id=" + history.getId() +
                    ", scheduled=" + history.getScheduledTime().format(FORMATTER) +
                    ", old status=" + history.getStatus());

            history.setStatus(MedicineStatus.PENDING);
            history.setNotes("Повторное напоминание");

            System.out.println("   → Новый статус: " + history.getStatus());
        }

        if (!postponedHistories.isEmpty()) {
            historyRepository.saveAll(postponedHistories);
            System.out.println("✅ Активировано POSTPONED записей: " + postponedHistories.size());
        }
    }

    @Override
    @Transactional
    public void markReminderAsTakenByBot(Long reminderId, Long telegramId) {
        System.out.println("🟢 markReminderAsTakenByBot: reminderId=" + reminderId + ", telegramId=" + telegramId);

        Reminder reminder = reminderRepository.findById(reminderId)
                .orElseThrow(() -> new RuntimeException("Reminder not found: " + reminderId));

        if (!reminder.getUser().getTelegramId().equals(telegramId)) {
            System.err.println("❌ Ошибка: Telegram ID не совпадает! expected=" +
                    reminder.getUser().getTelegramId() + ", actual=" + telegramId);
            throw new RuntimeException("Telegram ID does not match reminder owner");
        }

        LocalDate today = LocalDate.now();
        LocalDateTime startOfDay = today.atStartOfDay();
        LocalDateTime endOfDay = today.plusDays(1).atStartOfDay().minusNanos(1);

        List<MedicineHistory> histories = historyRepository.findByUserAndPeriodWithFetch(
                reminder.getUser(), startOfDay, endOfDay);

        System.out.println("   → Найдено записей за сегодня: " + histories.size());

        MedicineHistory history = histories.stream()
                .filter(h -> h.getReminder().getId().equals(reminderId))
                .findFirst()
                .orElse(null);

        if (history == null) {
            LocalDateTime scheduledTime = today.atTime(reminder.getReminderTime());
            history = new MedicineHistory(reminder, scheduledTime);
            history = historyRepository.save(history);
            System.out.println("   → Создана новая запись: id=" + history.getId());
        }

        history.markAsTaken();
        historyRepository.save(history);

        System.out.println("✅ Напоминание " + reminderId + " отмечено как принято через бота");
    }
}