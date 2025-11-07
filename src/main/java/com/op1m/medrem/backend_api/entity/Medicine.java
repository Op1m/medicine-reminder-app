package com.op1m.medrem.backend_api.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "medicines")
public class Medicine {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    private String dosage;
    private String description;
    private String instructions;

    @Column(name = "is_active")
    private boolean isActive = true;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @ManyToMany
    @JoinTable(
            name = "medicine_categories",
            joinColumns = @JoinColumn(name = "medicine_id"),
            inverseJoinColumns = @JoinColumn(name = "category_id")
    )
    private Set<Category> categories = new HashSet<>();

    public Medicine () {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public Medicine(String name, String dosage) {
        this();
        this.name = name;
        this.dosage = dosage;
    }

    public void setName (String name) {
        this.name = name;
        this.updatedAt = LocalDateTime.now();
    }

    public void setDosage (String dosage) {
        this.dosage = dosage;
        this.updatedAt = LocalDateTime.now();
    }

    public void setDescription (String description) {
        this.description = description;
        this.updatedAt = LocalDateTime.now();
    }

    public void setInstructions (String instructions) {
        this.instructions = instructions;
        this.updatedAt = LocalDateTime.now();
    }

    public void deactivate() {
        this.isActive = false;
        this.updatedAt = LocalDateTime.now();
    }

    public void activate() {
        this.isActive = true;
        this.updatedAt = LocalDateTime.now();
    }

    public Long getId () {return  id;}
    public String getName () {return  name;}
    public String getDosage () {return dosage;}
    public String getDescription () {return description;}
    public String getInstructions () {return instructions;}
    public boolean isActive () {return isActive;};
    public LocalDateTime getCreatedAt() {return createdAt;}
    public LocalDateTime getUpdatedAt() {return  updatedAt;}

    protected void setId(Long id) {this.id = id;}

    public void setCreatedAt(LocalDateTime createdAt) {this.createdAt = createdAt;}
    public  void setUpdatedAt(LocalDateTime updatedAt) {this.updatedAt = updatedAt;}
    public void setActive(Boolean isActive) {this.isActive = isActive;}

    public Set<Category> getCategories() { return categories; }
    public void setCategories(Set<Category> categories) { this.categories = categories; }

    public void addCategory(Category category) {
        this.categories.add(category);
        category.getMedicines().add(this);
    }

    public void removeCategory(Category category) {
        this.categories.remove(category);
        category.getMedicines().remove(this);
    }
}
