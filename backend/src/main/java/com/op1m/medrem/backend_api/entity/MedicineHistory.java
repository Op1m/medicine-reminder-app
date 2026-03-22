package com.op1m.medrem.backend_api.entity;

import com.op1m.medrem.backend_api.entity.enums.MedicineStatus;
import jakarta.persistence.*;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

@Entity
@Table(name = "medicine_history")
public class MedicineHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reminder_id", nullable = false)
    private Reminder reminder;

    @Column(name = "scheduled_time", nullable = false)
    private OffsetDateTime scheduledTime;

    @Column(name = "taken_at")
    private OffsetDateTime takenAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private MedicineStatus status = MedicineStatus.PENDING;

    @Column(name = "notes", length = 500)
    private String notes;

    @Column(name = "created_at")
    private OffsetDateTime createdAt;

    public MedicineHistory() {
        this.createdAt = OffsetDateTime.now(ZoneOffset.UTC);
    }

    public MedicineHistory(Reminder reminder, OffsetDateTime scheduledTime) {
        this();
        this.reminder = reminder;
        this.scheduledTime = scheduledTime;
        this.status = MedicineStatus.PENDING;
    }

    public void markAsTaken() {
        this.status = MedicineStatus.TAKEN;
        this.takenAt = OffsetDateTime.now(ZoneOffset.UTC);
    }

    public void markAsSkipped() {
        this.status = MedicineStatus.SKIPPED;
    }

    public void markAsMissed() {
        this.status = MedicineStatus.MISSED;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Reminder getReminder() { return reminder; }
    public void setReminder(Reminder reminder) { this.reminder = reminder; }

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