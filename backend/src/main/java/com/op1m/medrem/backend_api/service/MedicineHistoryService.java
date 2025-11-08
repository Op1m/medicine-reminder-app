package com.op1m.medrem.backend_api.service;

import com.op1m.medrem.backend_api.entity.MedicineHistory;
import com.op1m.medrem.backend_api.entity.enums.MedicineStatus;
import java.time.LocalDateTime;
import java.util.List;

public interface MedicineHistoryService {
    MedicineHistory createScheduleDose(Long reminderId, LocalDateTime scheduledTime);
    MedicineHistory markAsTaken(Long historyId, String notes);
    MedicineHistory markAsSkipped(Long historyId);
    List<MedicineHistory> getUserMedicineHistory(Long userId);
    List<MedicineHistory> getMedicineHistoryByStatus(Long userId, MedicineStatus status);
    List<MedicineHistory> getHistoryByPeriod(Long userId, LocalDateTime start, LocalDateTime end);
    void checkAndMarkMissedDoses();
}
