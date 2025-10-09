package com.op1m.medrem.backend_api.dto;

import com.op1m.medrem.backend_api.entity.enums.MedicineStatus;

import java.time.LocalDateTime;

public class MedicineHistoryDTO {
    private Long id;
    private ReminderDTO reminder;
    private LocalDateTime scheduledTime;
    private LocalDateTime takenAt;
    private MedicineStatus status;
    private String notes;
    private LocalDateTime createdAt;

    public MedicineHistoryDTO() {}

    public MedicineHistoryDTO(Long id, ReminderDTO reminder, LocalDateTime scheduledTime,
                              LocalDateTime takenAt, MedicineStatus status, String notes,
                              LocalDateTime createdAt) {
        this.id = id;
        this.reminder = reminder;
        this.scheduledTime = scheduledTime;
        this.takenAt = takenAt;
        this.status = status;
        this.notes = notes;
        this.createdAt = createdAt;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public ReminderDTO getReminder() { return reminder; }
    public void setReminder(ReminderDTO reminder) { this.reminder = reminder; }

    public LocalDateTime getScheduledTime() { return scheduledTime; }
    public void setScheduledTime(LocalDateTime scheduledTime) { this.scheduledTime = scheduledTime; }

    public LocalDateTime getTakenAt() { return takenAt; }
    public void setTakenAt(LocalDateTime takenAt) { this.takenAt = takenAt; }

    public MedicineStatus getStatus() { return status; }
    public void setStatus(MedicineStatus status) { this.status = status; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
