package com.op1m.medrem.backend_api.entity;

import jakarta.persistence.*;

import java.time.LocalTime;
import  java.time.LocalDateTime;

@Entity
@Table(name = "reminders")
public class Reminder {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "medicine", nullable = false)
    private Medicine medicine;

    @Column(name = "reminder_time", nullable = false)
    private LocalTime reminderTime;

    @Column(name = "is_active")
    private Boolean isActive = true;

    @Column(name = "days_of_week")
    private String daysOfWeek = "everyday";

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public Reminder() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public Reminder(User user, Medicine medicine, LocalTime reminderTime) {
        this();
        this.user = user;
        this.medicine = medicine;
        this.reminderTime = reminderTime;
    }

    public void setReminderTime (LocalTime reminderTime) {
        this.reminderTime = reminderTime;
        this.updatedAt = LocalDateTime.now();
    }

    public void setActive (Boolean isActive) {
        this.isActive = isActive;
        this.updatedAt = LocalDateTime.now();
    }

    public void setDaysOfWeek (String daysOfWeek) {
        this.daysOfWeek = daysOfWeek;
        this.updatedAt = LocalDateTime.now();
    }

    public Long getId () {return id;}
    public User getUser () {return user;}
    public Medicine getMedicine () {return medicine;}
    public LocalTime getReminderTime () {return reminderTime;}
    public Boolean getIsActive () {return isActive;}
    public String getDaysOfWeek () {return daysOfWeek;}
    public LocalDateTime getCreatedAt () {return createdAt;}
    public LocalDateTime getUpdatedAt () {return updatedAt;}

    public void setId (Long id) {this.id = id;}
    public void setUser (User user) {this.user = user;}
    public void setMedicine (Medicine medicine) {this.medicine = medicine;}
    public void setCreatedAt (LocalDateTime createdAt) {this.createdAt = createdAt;}
    public void setUpdatedAt (LocalDateTime updatedAt) {this.updatedAt = updatedAt;}
}
