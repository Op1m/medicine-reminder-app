package com.op1m.medrem.backend_api.service.impl;

import com.op1m.medrem.backend_api.entity.*;
import com.op1m.medrem.backend_api.entity.enums.MedicineStatus;
import com.op1m.medrem.backend_api.repository.MedicineHistoryRepository;
import com.op1m.medrem.backend_api.repository.ReminderRepository;
import com.op1m.medrem.backend_api.service.MedicineHistoryService;
import com.op1m.medrem.backend_api.service.NotificationService;
import com.op1m.medrem.backend_api.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;

@Service
public class MedicineHistoryServiceImpl implements MedicineHistoryService {

    @Autowired
    private MedicineHistoryRepository historyRepository;

    @Autowired
    private ReminderRepository reminderRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private NotificationService notificationService;

    @Override
    @Transactional
    public MedicineHistory createScheduleDose(Long reminderId, OffsetDateTime scheduledTime) {
        Reminder reminder = reminderRepository.findById(reminderId)
                .orElseThrow(() -> new RuntimeException("Reminder not found: " + reminderId));

        MedicineHistory history = new MedicineHistory();
        history.setReminder(reminder);
        history.setScheduledTime(scheduledTime != null ? scheduledTime : OffsetDateTime.now(ZoneOffset.UTC));
        history.setStatus(MedicineStatus.PENDING);

        return historyRepository.save(history);
    }

    @Override
    @Transactional
    public MedicineHistory markAsTaken(Long historyId, String notes) {
        MedicineHistory history = historyRepository.findById(historyId)
                .orElseThrow(() -> new RuntimeException("History not found: " + historyId));

        history.markAsTaken();
        if (notes != null) {
            history.setNotes(notes);
        }
        return historyRepository.save(history);
    }

    @Override
    @Transactional
    public MedicineHistory markAsSkipped(Long historyId) {
        MedicineHistory history = historyRepository.findWithReminderAndRelationsById(historyId);
        if (history == null) {
            throw new RuntimeException("MedicineHistory not found: " + historyId);
        }
        history.markAsSkipped();
        return historyRepository.save(history);
    }

    @Override
    public List<MedicineHistory> getUserMedicineHistory(Long userId) {
        User user = userService.findById(userId);
        if (user == null) throw new RuntimeException("User not found");
        return historyRepository.findByReminderUserOrderByScheduledTimeDesc(user);
    }

    @Override
    public List<MedicineHistory> getMedicineHistoryByStatus(Long userId, MedicineStatus status) {
        User user = userService.findById(userId);
        if (user == null) throw new RuntimeException("User not found");
        return historyRepository.findByReminderUserAndStatusOrderByScheduledTimeDesc(user, status);
    }

    @Override
    @Transactional(readOnly = true)
    public List<MedicineHistory> getHistoryByPeriod(Long userId, OffsetDateTime start, OffsetDateTime end) {
        User user = userService.findById(userId);
        if (user == null) throw new RuntimeException("User not found");
        return historyRepository.findByUserAndPeriod(user, start, end);
    }

    @Override
    @Transactional
    public void checkAndMarkMissedDoses() {
        OffsetDateTime threshold = OffsetDateTime.now(ZoneOffset.UTC).minusMinutes(10);
        List<MedicineHistory> pendingHistories = historyRepository.findByStatusAndScheduledTimeBefore(
                MedicineStatus.PENDING, threshold
        );
        for (MedicineHistory history : pendingHistories) {
            history.markAsMissed();
        }
        historyRepository.saveAll(pendingHistories);
    }

    @Override
    @Transactional
    public MedicineHistory postponeReminder(Long reminderId, Long chatId, int minutes) {
        Reminder reminder = reminderRepository.findById(reminderId)
                .orElseThrow(() -> new RuntimeException("Reminder not found: " + reminderId));

        if (!reminder.getUser().getTelegramChatId().equals(chatId)) {
            throw new RuntimeException("Chat ID does not match reminder owner");
        }

        LocalDate today = LocalDate.now(ZoneOffset.UTC);
        OffsetDateTime startOfDay = today.atStartOfDay().atOffset(ZoneOffset.UTC);
        OffsetDateTime endOfDay = startOfDay.plusDays(1).minusNanos(1);

        List<MedicineHistory> todayHistories = historyRepository.findByUserAndPeriod(reminder.getUser(), startOfDay, endOfDay);
        MedicineHistory postponedHistory = todayHistories.stream()
                .filter(h -> h.getReminder().getId().equals(reminderId))
                .findFirst()
                .orElse(null);

        if (postponedHistory == null) {
            OffsetDateTime scheduledTime = today.atTime(reminder.getReminderTime()).atOffset(ZoneOffset.UTC);
            postponedHistory = new MedicineHistory(reminder, scheduledTime);
            postponedHistory.setStatus(MedicineStatus.POSTPONED);
            postponedHistory = historyRepository.save(postponedHistory);
        } else {
            if (postponedHistory.getStatus() == MedicineStatus.PENDING) {
                postponedHistory.setStatus(MedicineStatus.POSTPONED);
                postponedHistory = historyRepository.save(postponedHistory);
            }
        }

        OffsetDateTime newScheduledTime = OffsetDateTime.now(ZoneOffset.UTC).plusMinutes(minutes);
        MedicineHistory reminderHistory = new MedicineHistory(reminder, newScheduledTime);
        reminderHistory.setStatus(MedicineStatus.PENDING);
        reminderHistory.setNotes("Повторное напоминание после откладывания");
        historyRepository.save(reminderHistory);

        return postponedHistory;
    }

    @Override
    @Transactional
    public void markReminderAsTakenByBot(Long reminderId, Long chatId) {
        Reminder reminder = reminderRepository.findById(reminderId)
                .orElseThrow(() -> new RuntimeException("Reminder not found: " + reminderId));

        if (!reminder.getUser().getTelegramChatId().equals(chatId)) {
            throw new RuntimeException("Chat ID does not match reminder owner");
        }

        OffsetDateTime startOfDay = OffsetDateTime.now(ZoneOffset.UTC).withHour(0).withMinute(0).withSecond(0).withNano(0);
        OffsetDateTime endOfDay = startOfDay.plusDays(1).minusNanos(1);

        List<MedicineHistory> histories = historyRepository.findByUserAndPeriod(
                reminder.getUser(), startOfDay, endOfDay);

        MedicineHistory history = histories.stream()
                .filter(h -> h.getReminder().getId().equals(reminderId))
                .findFirst()
                .orElse(null);

        if (history == null) {
            OffsetDateTime scheduledTime = startOfDay.withHour(reminder.getReminderTime().getHour())
                    .withMinute(reminder.getReminderTime().getMinute());
            history = new MedicineHistory(reminder, scheduledTime);
            history = historyRepository.save(history);
        }

        history.markAsTaken();
        historyRepository.save(history);

        System.out.println("✅ Напоминание " + reminderId + " отмечено как принято через бота");
    }

    @Override
    public MedicineHistory findById(Long historyId) {
        return historyRepository.findById(historyId)
                .orElseThrow(() -> new RuntimeException("History not found: " + historyId));
    }

    @Override
    public MedicineHistory save(MedicineHistory history) {
        return historyRepository.save(history);
    }

    @Override
    @Transactional
    public void markReminderAsSkippedByBot(Long reminderId, Long chatId) {
        Reminder reminder = reminderRepository.findById(reminderId)
                .orElseThrow(() -> new RuntimeException("Reminder not found: " + reminderId));

        if (!reminder.getUser().getTelegramChatId().equals(chatId)) {
            throw new RuntimeException("Chat ID does not match reminder owner");
        }

        OffsetDateTime startOfDay = OffsetDateTime.now(ZoneOffset.UTC).withHour(0).withMinute(0).withSecond(0).withNano(0);
        OffsetDateTime endOfDay = startOfDay.plusDays(1).minusNanos(1);

        List<MedicineHistory> histories = historyRepository.findByUserAndPeriod(
                reminder.getUser(), startOfDay, endOfDay);

        MedicineHistory history = histories.stream()
                .filter(h -> h.getReminder().getId().equals(reminderId))
                .findFirst()
                .orElse(null);

        if (history == null) {
            OffsetDateTime scheduledTime = startOfDay.withHour(reminder.getReminderTime().getHour())
                    .withMinute(reminder.getReminderTime().getMinute());
            history = new MedicineHistory(reminder, scheduledTime);
            history = historyRepository.save(history);
        }

        history.markAsSkipped();
        historyRepository.save(history);

        System.out.println("✅ Напоминание " + reminderId + " отмечено как пропущено через бота");
    }

    @Override
    @Transactional
    public void checkPostponedReminders() {
        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);
        OffsetDateTime oneMinuteAgo = now.minusMinutes(1);

        List<MedicineHistory> postponedHistories = historyRepository.findByStatusAndScheduledTimeBetween(
                MedicineStatus.POSTPONED, oneMinuteAgo, now);

        System.out.println("🔍 Проверка отложенных напоминаний: найдено " + postponedHistories.size() + " записей, время сейчас: " + now);

        for (MedicineHistory history : postponedHistories) {
            System.out.println("⏰ Активация отложенного напоминания ID: " + history.getId() +
                ", запланированное время: " + history.getScheduledTime());

            history.setStatus(MedicineStatus.PENDING);
            history.setNotes("Повторное напоминание");

            Reminder reminder = history.getReminder();
            if (reminder != null) {
                notificationService.notifyUser(reminder);
                System.out.println("📢 Отправлено повторное уведомление для напоминания: " + reminder.getId());
            }
        }
        historyRepository.saveAll(postponedHistories);
    }
}