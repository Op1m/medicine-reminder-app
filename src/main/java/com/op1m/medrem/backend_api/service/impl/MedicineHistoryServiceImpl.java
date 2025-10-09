package com.op1m.medrem.backend_api.service.impl;

import com.op1m.medrem.backend_api.entity.*;
import com.op1m.medrem.backend_api.entity.enums.MedicineStatus;
import com.op1m.medrem.backend_api.repository.MedicineHistoryRepository;
import com.op1m.medrem.backend_api.service.MedicineHistoryService;
import com.op1m.medrem.backend_api.service.ReminderService;
import com.op1m.medrem.backend_api.service.UserService;
import org.hibernate.query.sql.internal.ParameterRecognizerImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.security.PublicKey;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class MedicineHistoryServiceImpl implements MedicineHistoryService {
    @Autowired
    private MedicineHistoryRepository historyRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private ReminderService reminderService;

    @Override
    public MedicineHistory createScheduleDose(Long reminderId, LocalDateTime scheduledTime) {
        Reminder reminder = reminderService.findById(reminderId);
        MedicineHistory medicineHistory = new MedicineHistory(reminder, scheduledTime);
        MedicineHistory historySaved = historyRepository.save(medicineHistory);
        return historySaved;
    }

    @Override
    public MedicineHistory markAsTaken(Long historyId, String notes) {
        MedicineHistory medicineHistory = historyRepository.findById(historyId).orElseThrow(() -> new RuntimeException("History record not found"));;
        medicineHistory.markAsTaken();
        medicineHistory.setNotes(notes);
        return historyRepository.save(medicineHistory);
    }

    @Override
    public MedicineHistory markAsSkipped(Long historyId) {
        MedicineHistory medicineHistory = historyRepository.findById(historyId).orElseThrow(() -> new RuntimeException("History record not found"));;
        medicineHistory.markAsSkipped();
        return historyRepository.save(medicineHistory);
    }

    @Override
    public List<MedicineHistory> getUserMedicineHistory(Long userId) {
        User user = userService.findById(userId);
        if(user == null) {
            throw new RuntimeException("User not found");
        }
        return  historyRepository.findByReminderUserOrderByScheduledTimeDesc(user);
    }

    @Override
    public List<MedicineHistory> getMedicineHistoryByStatus(Long userId, MedicineStatus status) {
        User user = userService.findById(userId);
        if(user == null) {
            throw new RuntimeException("User not found");
        }
        return  historyRepository.findByReminderUserAndStatusOrderByScheduledTimeDesc(user, status);
    }

    @Override
    public List<MedicineHistory> getHistoryByPeriod(Long userId, LocalDateTime start, LocalDateTime end) {
        User user = userService.findById(userId);
        if(user == null) {
            throw new RuntimeException("User not found");
        }
        return  historyRepository.findByUserAndPeriod(user, start, end);
    }

    @Override
    public void checkAndMarkMissedDoses() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime threshold = now.minusHours(2);

        List<MedicineHistory> pendingDoses = historyRepository.findByStatusAndScheduledTimeBefore(MedicineStatus.PENDING, threshold);

        for (MedicineHistory medicineHistory : pendingDoses) {
            medicineHistory.markAsMissed();
            historyRepository.save(medicineHistory);
            System.out.println("⚠️ Помечен как пропущенный: " + medicineHistory.getId());
        }
    }
}
