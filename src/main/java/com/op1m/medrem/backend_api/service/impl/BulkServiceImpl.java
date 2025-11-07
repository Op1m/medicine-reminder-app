package com.op1m.medrem.backend_api.service.impl;

import com.op1m.medrem.backend_api.dto.BulkDeleteResponse;
import com.op1m.medrem.backend_api.entity.MedicineHistory;
import com.op1m.medrem.backend_api.entity.enums.MedicineStatus;
import com.op1m.medrem.backend_api.entity.Reminder;
import com.op1m.medrem.backend_api.repository.MedicineHistoryRepository;
import com.op1m.medrem.backend_api.repository.ReminderRepository;
import com.op1m.medrem.backend_api.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class BulkServiceImpl implements BulkService {

    @Autowired
    private ReminderService reminderService;

    @Autowired
    private MedicineHistoryService medicineHistoryService;

    @Autowired
    private UserService userService;

    @Autowired
    private MedicineService medicineService;

    @Autowired
    private ReminderRepository reminderRepository;

    @Autowired
    private MedicineHistoryRepository medicineHistoryRepository;

    @Override
    @Transactional
    public List<Reminder> createBulkReminders(Long userId, List<Long> medicineIds, LocalTime reminderTime, String daysOfWeek) {
        System.out.println("🔄 BulkService: Массовое создание напоминаний для " + medicineIds.size() + " лекарств");

        List<Reminder> createdReminders = new ArrayList<>();

        for (Long medicineId : medicineIds) {
            try {
                Reminder reminder = reminderService.createReminder(userId, medicineId, reminderTime, daysOfWeek);
                createdReminders.add(reminder);
                System.out.println("✅ Создано напоминание для medicineId: " + medicineId);
            } catch (RuntimeException e) {
                System.out.println("❌ Ошибка создания напоминания для medicineId: " + medicineId + " - " + e.getMessage());
            }
        }

        System.out.println("✅ BulkService: Успешно создано напоминаний: " + createdReminders.size());
        return createdReminders;
    }

    @Override
    @Transactional
    public List<MedicineHistory> markBulkAsTaken(List<Long> historyIds, String notes) {
        System.out.println("🔄 BulkService: Массовое отметка как принятых для " + historyIds.size() + " записей");

        List<MedicineHistory> updatedHistories = new ArrayList<>();

        for (Long historyId : historyIds) {
            try {
                MedicineHistory history = medicineHistoryService.markAsTaken(historyId, notes);
                updatedHistories.add(history);
                System.out.println("✅ Отмечено как принято: " + historyId);
            } catch (RuntimeException e) {
                System.out.println("❌ Ошибка отметки historyId: " + historyId + " - " + e.getMessage());
            }
        }

        System.out.println("✅ BulkService: Успешно отмечено записей: " + updatedHistories.size());
        return updatedHistories;
    }

    @Override
    @Transactional
    public List<MedicineHistory> markBulkAsSkipped(List<Long> historyIds) {
        System.out.println("🔄 BulkService: Массовое отметка как пропущенных для " + historyIds.size() + " записей");

        List<MedicineHistory> updatedHistories = new ArrayList<>();

        for (Long historyId : historyIds) {
            try {
                MedicineHistory history = medicineHistoryService.markAsSkipped(historyId);
                updatedHistories.add(history);
                System.out.println("✅ Отмечено как пропущено: " + historyId);
            } catch (RuntimeException e) {
                System.out.println("❌ Ошибка отметки historyId: " + historyId + " - " + e.getMessage());
            }
        }

        System.out.println("✅ BulkService: Успешно отмечено записей: " + updatedHistories.size());
        return updatedHistories;
    }

    @Override
    @Transactional
    public BulkDeleteResponse deleteBulkReminders(List<Long> reminderIds) {
        System.out.println("🔄 BulkService: Массовое удаление " + reminderIds.size() + " напоминаний");

        int deletedCount = 0;
        List<Long> notFoundIds = new ArrayList<>();

        for (Long reminderId : reminderIds) {
            boolean isDeleted = reminderService.deleteReminder(reminderId);
            if (isDeleted) {
                deletedCount++;
                System.out.println("✅ Удалено напоминание: " + reminderId);
            } else {
                notFoundIds.add(reminderId);
                System.out.println("❌ Напоминание не найдено: " + reminderId);
            }
        }

        BulkDeleteResponse response = new BulkDeleteResponse(
                deletedCount,
                notFoundIds.size(),
                notFoundIds
        );

        System.out.println("✅ BulkService: Удалено напоминаний: " + deletedCount + ", не найдено: " + notFoundIds.size());
        return response;
    }

    @Override
    @Transactional
    public List<Reminder> toggleBulkReminders(List<Long> reminderIds, Boolean active) {
        System.out.println("🔄 BulkService: Массовое переключение " + reminderIds.size() + " напоминаний -> " + active);

        List<Reminder> updatedReminders = new ArrayList<>();

        for (Long reminderId : reminderIds) {
            try {
                Reminder reminder = reminderService.toggleReminder(reminderId, active);
                updatedReminders.add(reminder);
                System.out.println("✅ Переключено напоминание: " + reminderId + " -> " + active);
            } catch (RuntimeException e) {
                System.out.println("❌ Ошибка переключения reminderId: " + reminderId + " - " + e.getMessage());
            }
        }

        System.out.println("✅ BulkService: Успешно переключено напоминаний: " + updatedReminders.size());
        return updatedReminders;
    }

    @Override
    @Transactional
    public List<MedicineHistory> scheduleBulkHistoryForPeriod(Long reminderId, LocalDate startDate, LocalDate endDate) {
        System.out.println("🔄 BulkService: Массовое создание истории для напоминания " + reminderId + " с " + startDate + " по " + endDate);

        Reminder reminder = reminderService.findById(reminderId);
        if (reminder == null) {
            throw new RuntimeException("Напоминание не найдено: " + reminderId);
        }

        List<MedicineHistory> createdHistories = new ArrayList<>();
        LocalDate currentDate = startDate;

        while (!currentDate.isAfter(endDate)) {
            try {
                LocalDateTime scheduledTime = LocalDateTime.of(currentDate, reminder.getReminderTime());
                MedicineHistory history = medicineHistoryService.createScheduleDose(reminderId, scheduledTime);
                createdHistories.add(history);
                System.out.println("✅ Создана запись истории на: " + currentDate);
            } catch (RuntimeException e) {
                System.out.println("❌ Ошибка создания истории на: " + currentDate + " - " + e.getMessage());
            }

            currentDate = currentDate.plusDays(1);
        }

        System.out.println("✅ BulkService: Создано записей истории: " + createdHistories.size());
        return createdHistories;
    }
}