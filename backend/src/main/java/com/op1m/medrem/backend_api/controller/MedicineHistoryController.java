package com.op1m.medrem.backend_api.controller;

import com.op1m.medrem.backend_api.dto.MedicineHistoryDTO;
import com.op1m.medrem.backend_api.dto.DTOMapper;
import com.op1m.medrem.backend_api.entity.MedicineHistory;
import com.op1m.medrem.backend_api.entity.enums.MedicineStatus;
import com.op1m.medrem.backend_api.service.MedicineHistoryService;
import com.op1m.medrem.backend_api.service.MedicineService;
import jakarta.persistence.GeneratedValue;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.io.StringWriter;
import java.io.PrintWriter;
import java.time.OffsetDateTime;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.Map;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/medicine-history")
public class MedicineHistoryController {
    @Autowired
    private MedicineHistoryService medicineHistoryService;

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<MedicineHistoryDTO>> getUserHistory(@PathVariable Long userId) {
        try {
            List<MedicineHistory> history = medicineHistoryService.getUserMedicineHistory(userId);
            List<MedicineHistoryDTO> historyDTO = history.stream()
                    .map(DTOMapper::toMedicineHistoryDTO)
                    .collect(Collectors.toList());
            return new ResponseEntity<>(historyDTO, HttpStatus.OK);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping("/user/{userId}/status/{status}")
    public ResponseEntity<List<MedicineHistoryDTO>> getUserHistoryByStatus (@PathVariable Long userId, @PathVariable MedicineStatus status) {
        try {
            List<MedicineHistory> history = medicineHistoryService.getMedicineHistoryByStatus(userId, status);
            List<MedicineHistoryDTO> historyDTO = history.stream()
                    .map(DTOMapper::toMedicineHistoryDTO)
                    .collect(Collectors.toList());
            return new ResponseEntity<>(historyDTO, HttpStatus.OK);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping("/user/{userId}/period")
    public ResponseEntity<List<MedicineHistoryDTO>> getHistoryByPeriod (
            @PathVariable Long userId,
            @RequestParam String start,
            @RequestParam String end) {
        try {
            LocalDateTime startDt = parseToLocalDateTime(start);
            LocalDateTime endDt = parseToLocalDateTime(end);

            List<MedicineHistory> history = medicineHistoryService.getHistoryByPeriod(userId, startDt, endDt);
            List<MedicineHistoryDTO> historyDTO = history.stream()
                    .map(DTOMapper::toMedicineHistoryDTO)
                    .collect(Collectors.toList());
            return new ResponseEntity<>(historyDTO, HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().build();
        } catch (RuntimeException e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    private LocalDateTime parseToLocalDateTime(String s) {
        if (s == null) throw new IllegalArgumentException("date param is null");
        String trimmed = s.trim();
        try {
            OffsetDateTime odt = OffsetDateTime.parse(trimmed);
            return odt.toLocalDateTime();
        } catch (DateTimeParseException ex) {
            try {
                return LocalDateTime.parse(trimmed);
            } catch (DateTimeParseException ex2) {
                String fallback = trimmed;
                if (fallback.endsWith("Z")) fallback = fallback.substring(0, fallback.length() - 1);
                try {
                    return LocalDateTime.parse(fallback);
                } catch (DateTimeParseException ex3) {
                    throw new IllegalArgumentException("Cannot parse date: " + s);
                }
            }
        }
    }

    @PatchMapping("/{historyId}/mark-taken")
    public ResponseEntity<MedicineHistoryDTO> markAsTaken(@PathVariable Long historyId, @RequestBody(required = false)  MarkTakenRequest request) {
        try {
            String notes = request != null ? request.getNotes() : null;
            MedicineHistory history = medicineHistoryService.markAsTaken(historyId, notes);
            MedicineHistoryDTO historyDTO = DTOMapper.toMedicineHistoryDTO(history);
            return new ResponseEntity<>(historyDTO, HttpStatus.OK);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @PatchMapping("/{historyId}/mark-skipped")
    public ResponseEntity<MedicineHistoryDTO> markAsSkipped(@PathVariable Long historyId, @RequestBody(required = false)  MarkTakenRequest request) {
        try {
            MedicineHistory history = medicineHistoryService.markAsSkipped(historyId);
            MedicineHistoryDTO historyDTO = DTOMapper.toMedicineHistoryDTO(history);
            return new ResponseEntity<>(historyDTO, HttpStatus.OK);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @PostMapping("/schedule")
    public ResponseEntity<?> createScheduleDose(@RequestBody ScheduleDoseRequest request) {
        try {
            LocalDateTime scheduled = null;
            if (request.getScheduledTime() != null) {
                try {
                    scheduled = OffsetDateTime.parse(request.getScheduledTime()).toLocalDateTime();
                } catch (DateTimeParseException ex) {
                    scheduled = LocalDateTime.parse(request.getScheduledTime());
                }
            }
            MedicineHistory history = medicineHistoryService.createScheduleDose(request.getReminderId(), scheduled);
            MedicineHistoryDTO dto = DTOMapper.toMedicineHistoryDTO(history);
            return ResponseEntity.ok(dto);
        } catch (Exception e) {
            e.printStackTrace();
            Map<String,String> err = new HashMap<>();
            err.put("error", e.getMessage());
            err.put("trace", e.toString());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(err);
        }
    }

    private String stackTraceToString(Throwable t) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        t.printStackTrace(pw);
        pw.flush();
        return sw.toString();
    }

    @PostMapping("/check_missed")
    public ResponseEntity<Void> checkMissedDoses() {
        medicineHistoryService.checkAndMarkMissedDoses();
        return new ResponseEntity<>(HttpStatus.OK);
    }

    public static class MarkTakenRequest {
        private String notes;

        public String getNotes () {return notes;}
        public void setNotes(String notes) { this.notes = notes;}
    }

    public static class ScheduleDoseRequest {
        private Long reminderId;
        private String scheduledTime;

        public Long getReminderId() { return reminderId; }
        public void setReminderId(Long reminderId) { this.reminderId = reminderId; }
        public String getScheduledTime() { return scheduledTime; }
        public void setScheduledTime(String scheduledTime) { this.scheduledTime = scheduledTime; }
    }
}
