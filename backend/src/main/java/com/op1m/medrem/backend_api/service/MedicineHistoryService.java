package com.op1m.medrem.backend_api.service;

import com.op1m.medrem.backend_api.entity.MedicineHistory;
import com.op1m.medrem.backend_api.entity.enums.MedicineStatus;
import java.time.OffsetDateTime;
import java.util.List;

public interface MedicineHistoryService {
    MedicineHistory createScheduleDose(Long reminderId, OffsetDateTime scheduledTime);
    MedicineHistory markAsTaken(Long historyId, String notes);
    MedicineHistory markAsSkipped(Long historyId);
    List<MedicineHistory> getUserMedicineHistory(Long userId);
    List<MedicineHistory> getMedicineHistoryByStatus(Long userId, MedicineStatus status);
    List<MedicineHistory> getHistoryByPeriod(Long userId, OffsetDateTime start, OffsetDateTime end);
    void markReminderAsTakenByBot(Long reminderId, Long telegramId);
    MedicineHistory postponeReminder(Long reminderId, Long telegramId, int minutes);
    void markReminderAsSkippedByBot(Long reminderId, Long telegramId);
    void checkPostponedReminders();
    void checkAndMarkMissedDoses();
}