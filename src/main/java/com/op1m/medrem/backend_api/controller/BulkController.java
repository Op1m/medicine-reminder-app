package com.op1m.medrem.backend_api.controller;

import com.op1m.medrem.backend_api.dto.MedicineHistoryDTO;
import com.op1m.medrem.backend_api.dto.ReminderDTO;
import com.op1m.medrem.backend_api.dto.BulkDeleteResponse;
import com.op1m.medrem.backend_api.dto.DTOMapper;
import com.op1m.medrem.backend_api.entity.MedicineHistory;
import com.op1m.medrem.backend_api.entity.Reminder;
import com.op1m.medrem.backend_api.service.BulkService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/bulk")
public class BulkController {

    @Autowired
    private BulkService bulkService;

    @PostMapping("/reminders")
    public ResponseEntity<List<ReminderDTO>> createBulkReminders(@RequestBody BulkReminderCreateRequest request) {
        try {
            System.out.println("🔄 BulkController: Массовое создание напоминаний для пользователя: " + request.getUserId());

            List<Reminder> reminders = bulkService.createBulkReminders(
                    request.getUserId(),
                    request.getMedicineIds(),
                    request.getReminderTime(),
                    request.getDaysOfWeek()
            );

            List<ReminderDTO> reminderDTOs = reminders.stream()
                    .map(DTOMapper::toReminderDTO)
                    .collect(Collectors.toList());

            System.out.println("✅ BulkController: Создано напоминаний: " + reminders.size());
            return new ResponseEntity<>(reminderDTOs, HttpStatus.CREATED);
        } catch (RuntimeException e) {
            System.out.println("❌ BulkController: Ошибка массового создания: " + e.getMessage());
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    @PatchMapping("/history/mark-taken")
    public ResponseEntity<List<MedicineHistoryDTO>> markBulkAsTaken(@RequestBody BulkMarkTakenRequest request) {
        try {
            System.out.println("🔄 BulkController: Массовое отметка как принятых: " + request.getHistoryIds().size() + " записей");

            List<MedicineHistory> histories = bulkService.markBulkAsTaken(
                    request.getHistoryIds(),
                    request.getNotes()
            );

            List<MedicineHistoryDTO> historyDTOs = histories.stream()
                    .map(DTOMapper::toMedicineHistoryDTO)
                    .collect(Collectors.toList());

            System.out.println("✅ BulkController: Отмечено как принятых: " + histories.size());
            return new ResponseEntity<>(historyDTOs, HttpStatus.OK);
        } catch (RuntimeException e) {
            System.out.println("❌ BulkController: Ошибка массового отметки: " + e.getMessage());
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    @PatchMapping("/history/mark-skipped")
    public ResponseEntity<List<MedicineHistoryDTO>> markBulkAsSkipped(@RequestBody BulkMarkSkippedRequest request) {
        try {
            System.out.println("🔄 BulkController: Массовое отметка как пропущенных: " + request.getHistoryIds().size() + " записей");

            List<MedicineHistory> histories = bulkService.markBulkAsSkipped(request.getHistoryIds());

            List<MedicineHistoryDTO> historyDTOs = histories.stream()
                    .map(DTOMapper::toMedicineHistoryDTO)
                    .collect(Collectors.toList());

            System.out.println("✅ BulkController: Отмечено как пропущенных: " + histories.size());
            return new ResponseEntity<>(historyDTOs, HttpStatus.OK);
        } catch (RuntimeException e) {
            System.out.println("❌ BulkController: Ошибка массового отметки: " + e.getMessage());
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    @DeleteMapping("/reminders")
    public ResponseEntity<BulkDeleteResponse> deleteBulkReminders(@RequestBody BulkDeleteRequest request) {
        try {
            System.out.println("🔄 BulkController: Массовое удаление напоминаний: " + request.getReminderIds().size() + " шт");

            BulkDeleteResponse response = bulkService.deleteBulkReminders(request.getReminderIds());

            System.out.println("✅ BulkController: Удалено напоминаний: " + response.getDeletedCount());
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (RuntimeException e) {
            System.out.println("❌ BulkController: Ошибка массового удаления: " + e.getMessage());
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    // 🆕 МАССОВОЕ ВКЛЮЧЕНИЕ/ВЫКЛЮЧЕНИЕ НАПОМИНАНИЙ
    @PatchMapping("/reminders/toggle")
    public ResponseEntity<List<ReminderDTO>> toggleBulkReminders(@RequestBody BulkToggleRequest request) {
        try {
            System.out.println("🔄 BulkController: Массовое переключение напоминаний: " + request.getReminderIds().size() + " шт -> " + request.getActive());

            List<Reminder> reminders = bulkService.toggleBulkReminders(
                    request.getReminderIds(),
                    request.getActive()
            );

            List<ReminderDTO> reminderDTOs = reminders.stream()
                    .map(DTOMapper::toReminderDTO)
                    .collect(Collectors.toList());

            System.out.println("✅ BulkController: Переключено напоминаний: " + reminders.size());
            return new ResponseEntity<>(reminderDTOs, HttpStatus.OK);
        } catch (RuntimeException e) {
            System.out.println("❌ BulkController: Ошибка массового переключения: " + e.getMessage());
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping("/history/schedule-period")
    public ResponseEntity<List<MedicineHistoryDTO>> scheduleBulkHistory(@RequestBody BulkSchedulePeriodRequest request) {
        try {
            System.out.println("🔄 BulkController: Массовое создание истории на период для напоминания: " + request.getReminderId());

            List<MedicineHistory> histories = bulkService.scheduleBulkHistoryForPeriod(
                    request.getReminderId(),
                    request.getStartDate(),
                    request.getEndDate()
            );

            List<MedicineHistoryDTO> historyDTOs = histories.stream()
                    .map(DTOMapper::toMedicineHistoryDTO)
                    .collect(Collectors.toList());

            System.out.println("✅ BulkController: Создано записей истории: " + histories.size());
            return new ResponseEntity<>(historyDTOs, HttpStatus.CREATED);
        } catch (RuntimeException e) {
            System.out.println("❌ BulkController: Ошибка массового создания истории: " + e.getMessage());
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    public static class BulkReminderCreateRequest {
        private Long userId;
        private List<Long> medicineIds;
        private LocalTime reminderTime;
        private String daysOfWeek = "everyday";

        public Long getUserId() { return userId; }
        public void setUserId(Long userId) { this.userId = userId; }

        public List<Long> getMedicineIds() { return medicineIds; }
        public void setMedicineIds(List<Long> medicineIds) { this.medicineIds = medicineIds; }

        public LocalTime getReminderTime() { return reminderTime; }
        public void setReminderTime(LocalTime reminderTime) { this.reminderTime = reminderTime; }

        public String getDaysOfWeek() { return daysOfWeek; }
        public void setDaysOfWeek(String daysOfWeek) { this.daysOfWeek = daysOfWeek; }
    }

    public static class BulkMarkTakenRequest {
        private List<Long> historyIds;
        private String notes;

        public List<Long> getHistoryIds() { return historyIds; }
        public void setHistoryIds(List<Long> historyIds) { this.historyIds = historyIds; }

        public String getNotes() { return notes; }
        public void setNotes(String notes) { this.notes = notes; }
    }

    public static class BulkMarkSkippedRequest {
        private List<Long> historyIds;

        public List<Long> getHistoryIds() { return historyIds; }
        public void setHistoryIds(List<Long> historyIds) { this.historyIds = historyIds; }
    }

    public static class BulkDeleteRequest {
        private List<Long> reminderIds;

        public List<Long> getReminderIds() { return reminderIds; }
        public void setReminderIds(List<Long> reminderIds) { this.reminderIds = reminderIds; }
    }

    public static class BulkToggleRequest {
        private List<Long> reminderIds;
        private Boolean active;

        public List<Long> getReminderIds() { return reminderIds; }
        public void setReminderIds(List<Long> reminderIds) { this.reminderIds = reminderIds; }

        public Boolean getActive() { return active; }
        public void setActive(Boolean active) { this.active = active; }
    }

    public static class BulkSchedulePeriodRequest {
        private Long reminderId;

        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        private LocalDate startDate;

        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        private LocalDate endDate;

        public Long getReminderId() { return reminderId; }
        public void setReminderId(Long reminderId) { this.reminderId = reminderId; }

        public LocalDate getStartDate() { return startDate; }
        public void setStartDate(LocalDate startDate) { this.startDate = startDate; }

        public LocalDate getEndDate() { return endDate; }
        public void setEndDate(LocalDate endDate) { this.endDate = endDate; }
    }
}