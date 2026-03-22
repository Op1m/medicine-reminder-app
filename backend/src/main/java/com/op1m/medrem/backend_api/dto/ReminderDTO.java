package com.op1m.medrem.backend_api.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalTime;
import java.time.OffsetDateTime;

public class ReminderDTO {
    private Long id;
    private UserDTO user;
    private MedicineDTO medicine;
    private LocalTime reminderTime;
    private Boolean isActive;
    private String daysOfWeek;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ssXXX")
    private OffsetDateTime createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ssXXX")
    private OffsetDateTime updatedAt;

    public ReminderDTO() {}

    public ReminderDTO(Long id, UserDTO user, MedicineDTO medicine, LocalTime reminderTime,
                       Boolean isActive, String daysOfWeek,
                       OffsetDateTime createdAt, OffsetDateTime updatedAt) {
        this.id = id;
        this.user = user;
        this.medicine = medicine;
        this.reminderTime = reminderTime;
        this.isActive = isActive;
        this.daysOfWeek = daysOfWeek;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public UserDTO getUser() { return user; }
    public void setUser(UserDTO user) { this.user = user; }

    public MedicineDTO getMedicine() { return medicine; }
    public void setMedicine(MedicineDTO medicine) { this.medicine = medicine; }

    public LocalTime getReminderTime() { return reminderTime; }
    public void setReminderTime(LocalTime reminderTime) { this.reminderTime = reminderTime; }

    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }

    public String getDaysOfWeek() { return daysOfWeek; }
    public void setDaysOfWeek(String daysOfWeek) { this.daysOfWeek = daysOfWeek; }

    public OffsetDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }

    public OffsetDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(OffsetDateTime updatedAt) { this.updatedAt = updatedAt; }
}