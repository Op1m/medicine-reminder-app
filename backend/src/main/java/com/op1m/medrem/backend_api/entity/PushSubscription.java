package com.op1m.medrem.backend_api.entity;

import com.op1m.medrem.backend_api.entity.enums.MedicineStatus;
import jakarta.persistence.*;

@Entity
@Table(name = "push_subscriptions")
public class PushSubscription {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "endpoint", length = 512, nullable = false)
    private String endpoint;

    @Column(name = "p256dh", length = 256, nullable = false)
    private String p256dh;

    @Column(name = "auth", length = 256, nullable = false)
    private String auth;

    public PushSubscription() {}

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getEndpoint() {
        return endpoint;
    }

    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }

    public String getP256dh() {
        return p256dh;
    }

    public void setP256dh(String p256dh) {
        this.p256dh = p256dh;
    }

    public String getAuth() {
        return auth;
    }

    public void setAuth(String auth) {
        this.auth = auth;
    }

    @Override
    public String toString() {
        return "PushSubscription{" +
                "id=" + id +
                ", userId=" + (user != null ? user.getId() : null) +
                ", endpoint='" + endpoint + '\'' +
                '}';
    }
}