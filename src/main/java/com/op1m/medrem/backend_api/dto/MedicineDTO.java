package com.op1m.medrem.backend_api.dto;

import java.time.LocalDateTime;

public class MedicineDTO {
    private Long id;
    private String name;
    private String dosage;
    private String description;
    private String instructions;
    private boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public MedicineDTO() {}

    public MedicineDTO(Long id, String name, String dosage, String description,
                       String instructions, boolean isActive, LocalDateTime createdAt,
                       LocalDateTime updatedAt) {
        this.id = id;
        this.name = name;
        this.dosage = dosage;
        this.description = description;
        this.instructions = instructions;
        this.isActive = isActive;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDosage() { return dosage; }
    public void setDosage(String dosage) { this.dosage = dosage; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getInstructions() { return instructions; }
    public void setInstructions(String instructions) { this.instructions = instructions; }

    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
