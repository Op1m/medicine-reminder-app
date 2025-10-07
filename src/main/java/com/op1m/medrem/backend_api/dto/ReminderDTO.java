package com.op1m.medrem.backend_api.dto;

import java.time.LocalTime;
import  java.time.LocalDateTime;

public class ReminderDTO {
    private Long id;
    private UserDTO user;
    private MedicineDTO medicine;
    private LocalTime reminderTime;
    private Boolean isActive;
    private String daysOfWeek;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public ReminderDTO() {}

    public ReminderDTO(Long id, UserDTO user, MedicineDTO medicine, LocalTime reminderTime,
                       Boolean isActive, String daysOfWeek, LocalDateTime createdAt,
                       LocalDateTime updatedAt) {
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

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
