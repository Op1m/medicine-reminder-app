package com.op1m.medrem.backend_api.service.impl;

import com.op1m.medrem.backend_api.entity.*;
import com.op1m.medrem.backend_api.entity.enums.MedicineStatus;
import com.op1m.medrem.backend_api.repository.MedicineHistoryRepository;
import com.op1m.medrem.backend_api.repository.ReminderRepository;
import com.op1m.medrem.backend_api.service.MedicineHistoryService;
import com.op1m.medrem.backend_api.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    public MedicineHistory postponeReminder(Long reminderId, Long telegramId, int minutes) {
        Reminder reminder = reminderRepository.findById(reminderId)
                .orElseThrow(() -> new RuntimeException("Reminder not found: " + reminderId));

        if (!reminder.getUser().getTelegramId().equals(telegramId)) {
            throw new RuntimeException("Telegram ID does not match reminder owner");
        }

        OffsetDateTime newScheduledTime = OffsetDateTime.now(ZoneOffset.UTC).plusMinutes(minutes);

        MedicineHistory postponedHistory = new MedicineHistory(reminder, newScheduledTime);
        postponedHistory.setStatus(MedicineStatus.POSTPONED);
        postponedHistory.setNotes("Отложено на " + minutes + " минут");

        return historyRepository.save(postponedHistory);
    }

    @Override
    @Transactional
    public void markReminderAsSkippedByBot(Long reminderId, Long telegramId) {
        Reminder reminder = reminderRepository.findById(reminderId)
                .orElseThrow(() -> new RuntimeException("Reminder not found: " + reminderId));

        if (!reminder.getUser().getTelegramId().equals(telegramId)) {
            throw new RuntimeException("Telegram ID does not match reminder owner");
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
    }

    @Override
    @Transactional
    public void checkPostponedReminders() {
        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);
        OffsetDateTime oneMinuteAgo = now.minusMinutes(1);

        List<MedicineHistory> postponedHistories = historyRepository.findByStatusAndScheduledTimeBetween(
                MedicineStatus.POSTPONED, oneMinuteAgo, now);

        for (MedicineHistory history : postponedHistories) {
            history.setStatus(MedicineStatus.PENDING);
            history.setNotes("Повторное напоминание");
        }
        historyRepository.saveAll(postponedHistories);
    }

    @Override
    @Transactional
    public void markReminderAsTakenByBot(Long reminderId, Long telegramId) {
        Reminder reminder = reminderRepository.findById(reminderId)
                .orElseThrow(() -> new RuntimeException("Reminder not found: " + reminderId));

        if (!reminder.getUser().getTelegramId().equals(telegramId)) {
            throw new RuntimeException("Telegram ID does not match reminder owner");
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
    }
}