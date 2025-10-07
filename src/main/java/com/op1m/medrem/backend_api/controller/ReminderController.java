package com.op1m.medrem.backend_api.controller;

import com.op1m.medrem.backend_api.entity.Reminder;
import com.op1m.medrem.backend_api.service.ReminderService;
import com.op1m.medrem.backend_api.dto.DTOMapper;
import com.op1m.medrem.backend_api.dto.ReminderDTO;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/reminders")
public class ReminderController {
    @Autowired
    private ReminderService reminderService;

    @PostMapping
    public ResponseEntity<ReminderDTO> createReminder(@RequestBody ReminderCreateRequest request) {
        Reminder reminder = reminderService.createReminder(
                request.getUserId(),
                request.getMedicineId(),
                request.getReminderTime(),
                request.getDaysOfWeek()
        );
        ReminderDTO reminderDTO = DTOMapper.toReminderDTO(reminder);
        return new ResponseEntity<>(reminderDTO, HttpStatus.OK);
    }

    @GetMapping("/user/{userId}")
    public List<ReminderDTO> getUserReminders(@PathVariable Long userId) {
        return reminderService.getUserReminders(userId).stream().map(DTOMapper::toReminderDTO).collect(Collectors.toList());
    }

    @GetMapping("/user/{userId}/active")
    public List<ReminderDTO> getUserActiveReminders(@PathVariable Long userId) {
        return reminderService.getUserActiveReminders(userId).stream().map(DTOMapper::toReminderDTO).collect(Collectors.toList());
    }

    @PatchMapping("/{reminderId}/toggle")
    public ResponseEntity<ReminderDTO> toggleReminder (@PathVariable Long reminderId, @RequestParam Boolean active) {
        Reminder reminder = reminderService.toggleReminder(reminderId, active);
        ReminderDTO reminderDTO = DTOMapper.toReminderDTO(reminder);
        if(reminder != null) {
            return new ResponseEntity<>(reminderDTO, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @DeleteMapping("/{reminderId}")
    public ResponseEntity<Void> deleteReminder(@PathVariable Long reminderId) {
        boolean isDeleted = reminderService.deleteReminder(reminderId);
        if (isDeleted) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping("/due")
    public List<ReminderDTO> getDueReminders() {
        return reminderService.getDueReminders().stream().map(DTOMapper::toReminderDTO).collect(Collectors.toList());
    }

    @PutMapping("/{reminderId}/time")
    public ResponseEntity<ReminderDTO> updateReminderTime(@PathVariable Long reminderId, @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime newTime) {
        Reminder reminder = reminderService.updateReminderTime(reminderId, newTime);
        ReminderDTO reminderDTO = DTOMapper.toReminderDTO(reminder);
        if (reminder != null) {
            return new ResponseEntity<>(reminderDTO, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    public static class ReminderCreateRequest {
        private Long userId;
        private Long medicineId;

        @DateTimeFormat(iso = DateTimeFormat.ISO.TIME)
        private LocalTime reminderTime;

        private String daysOfWeek = "everyday";

        public Long getUserId() {return userId;}
        public void setUserId(Long userId) {this.userId = userId;}

        public Long getMedicineId() {return medicineId;}
        public void setMedicineId(Long medicineId) {this.medicineId = medicineId;}

        public LocalTime getReminderTime() {return reminderTime;}
        public void setReminderTime(LocalTime reminderTime) {this.reminderTime = reminderTime;}

        public String getDaysOfWeek() {return daysOfWeek;}
        public void setDaysOfWeek(String daysOfWeek) {this.daysOfWeek = daysOfWeek;}
    }
}
