package com.op1m.medrem.backend_api.service;

import com.op1m.medrem.backend_api.dto.BulkDeleteResponse;
import com.op1m.medrem.backend_api.entity.MedicineHistory;
import com.op1m.medrem.backend_api.entity.Reminder;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public interface BulkService {
    List<Reminder> createBulkReminders(Long userId, List<Long> medicineIds, LocalTime reminderTime, String daysOfWeek);
    List<MedicineHistory> markBulkAsTaken(List<Long> historyIds, String notes);
    List<MedicineHistory> markBulkAsSkipped(List<Long> historyIds);
    BulkDeleteResponse deleteBulkReminders(List<Long> reminderIds);
    List<Reminder> toggleBulkReminders(List<Long> reminderIds, Boolean active);
    List<MedicineHistory> scheduleBulkHistoryForPeriod(Long reminderId, LocalDate startDate, LocalDate endDate);
}