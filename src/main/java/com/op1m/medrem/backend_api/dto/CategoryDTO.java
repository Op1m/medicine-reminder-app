package com.op1m.medrem.backend_api.dto;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.stream.Collectors;

public class CategoryDTO {
    private Long id;
    private String name;
    private String description;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Set<MedicineDTO> medicines;

    public CategoryDTO() {}

    public CategoryDTO(Long id, String name, String description, Boolean isActive,
                       LocalDateTime createdAt, LocalDateTime updatedAt, Set<MedicineDTO> medicines) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.isActive = isActive;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.medicines = medicines;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public Set<MedicineDTO> getMedicines() { return medicines; }
    public void setMedicines(Set<MedicineDTO> medicines) { this.medicines = medicines; }
}