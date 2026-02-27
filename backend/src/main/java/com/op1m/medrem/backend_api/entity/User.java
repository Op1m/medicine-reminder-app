package com.op1m.medrem.backend_api.entity;

import com.op1m.medrem.backend_api.MedicineReminderApiApplication;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import  java.util.ArrayList;
import  java.util.List;

@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = true, unique = true)
    private String username;

    @Column(nullable = true)
    private String password;

    @Column(nullable = true, unique = true)
    private String email;

    private String first_name;
    private String last_name;

    @Column(name = "telegram_chat_id")
    private Long telegramChatId;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "is_active")
    private Boolean isActive = true;

    @Column(name = "telegram_id", unique = true)
    private Long telegramId;

    @Column(name = "photo_url")
    private String photoUrl;

    @Column(name = "language_code")
    private String languageCode;

    @Column(name = "last_login")
    private LocalDateTime lastLogin;


    @OneToMany(
            mappedBy = "user",
            cascade = CascadeType.ALL,
            fetch = FetchType.LAZY
    )

    private List<Reminder> reminders = new ArrayList<>();

    @PreUpdate
    public void preUpdate () {
        this.updatedAt = LocalDateTime.now();
    }

    @Override
    public String toString() {
        return "User{id=" + id + ", username='" + username + "', email='" + email + "'}";
    }

    public User() {
        this.createdAt = LocalDateTime.now();
    }

    public User(String username, String password, String email) {
        this.username = username;
        this.password = password;
        this.email = email;
    }

    @PrePersist
    protected void onCreate() {
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
        if (this.updatedAt == null) {
            this.updatedAt = LocalDateTime.now();
        }
    }

    public Long getId () {
        return id;
    }

    public void setId (Long id) {
        this.id = id;
    }

    public String getUsername () {
        return username;
    }

    public void setUsername (String username) {
        this.username = username;
    }

    public String getFirstName () {
        return first_name;
    }

    public void setFirstName (String first_name) {
        this.first_name = first_name;
    }

    public String getLastName () {return last_name; }

    public void setLastName (String last_name) {
        this.last_name = last_name;
    }

    public String getPassword () {
        return password;
    }

    public void setPassword (String password) {
        this.password = password;
    }

    public String getEmail () {
        return email;
    }

    public void setEmail (String email) {
        this.email = email;
    }

    public Long getTelegramChatId () {return telegramChatId;}

    public void setTelegramChatId (Long telegramChatId) {this.telegramChatId = telegramChatId;}

    public LocalDateTime getCreatedAt () {return createdAt;}
    public LocalDateTime getUpdatedAt () {return updatedAt;}
    public void setCreatedAt (LocalDateTime createdAt) {this.createdAt = createdAt;}
    public void setUpdatedAt (LocalDateTime updatedAt) {this.updatedAt = updatedAt;}

    public Boolean getActive() { return isActive; }
    public void setActive(Boolean active) { isActive = active; }

    public Long getTelegramId() { return telegramId; }
    public void setTelegramId(Long telegramId) { this.telegramId = telegramId; }

    public String getPhotoUrl() { return photoUrl; }
    public void setPhotoUrl(String photoUrl) { this.photoUrl = photoUrl; }

    public String getLanguageCode() { return languageCode; }
    public void setLanguageCode(String languageCode) { this.languageCode = languageCode; }

    public LocalDateTime getLastLogin() { return lastLogin; }
    public void setLastLogin(LocalDateTime lastLogin) { this.lastLogin = lastLogin; }
}