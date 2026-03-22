package com.op1m.medrem.backend_api.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.op1m.medrem.backend_api.entity.enums.MedicineStatus;
import java.time.OffsetDateTime;

public class MedicineHistoryDTO {
    private Long id;
    private ReminderDTO reminder;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ssXXX")
    private OffsetDateTime scheduledTime;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ssXXX")
    private OffsetDateTime takenAt;

    private MedicineStatus status;
    private String notes;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ssXXX")
    private OffsetDateTime createdAt;

    public MedicineHistoryDTO() {}

    public MedicineHistoryDTO(Long id, ReminderDTO reminder, OffsetDateTime scheduledTime,
                              OffsetDateTime takenAt, MedicineStatus status, String notes,
                              OffsetDateTime createdAt) {
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

    public OffsetDateTime getScheduledTime() { return scheduledTime; }
    public void setScheduledTime(OffsetDateTime scheduledTime) { this.scheduledTime = scheduledTime; }

    public OffsetDateTime getTakenAt() { return takenAt; }
    public void setTakenAt(OffsetDateTime takenAt) { this.takenAt = takenAt; }

    public MedicineStatus getStatus() { return status; }
    public void setStatus(MedicineStatus status) { this.status = status; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public OffsetDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }
}