package com.op1m.medrem.backend_api.controller;

import com.op1m.medrem.backend_api.dto.CategoryDTO;
import com.op1m.medrem.backend_api.dto.DTOMapper;
import com.op1m.medrem.backend_api.entity.Category;
import com.op1m.medrem.backend_api.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/categories")
public class CategoryController {

    @Autowired
    private CategoryService categoryService;

    @PostMapping
    public ResponseEntity<CategoryDTO> createCategory(@RequestBody CategoryCreateRequest request) {
        try {
            Category category = categoryService.createCategory(request.getName(), request.getDescription());
            CategoryDTO categoryDTO = DTOMapper.toCategoryDTOSimple(category);
            return new ResponseEntity<>(categoryDTO, HttpStatus.CREATED);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(HttpStatus.CONFLICT);
        }
    }

    @GetMapping
    public List<CategoryDTO> getAllActiveCategories() {
        return categoryService.getAllActiveCategories().stream()
                .map(DTOMapper::toCategoryDTOSimple)
                .collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    public ResponseEntity<CategoryDTO> getCategoryById(@PathVariable Long id) {
        try {
            Category category = categoryService.findById(id);
            CategoryDTO categoryDTO = DTOMapper.toCategoryDTO(category);
            return new ResponseEntity<>(categoryDTO, HttpStatus.OK);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping("/search")
    public List<CategoryDTO> searchCategories(@RequestParam String name) {
        return categoryService.searchCategories(name).stream()
                .map(DTOMapper::toCategoryDTOSimple)
                .collect(Collectors.toList());
    }

    @GetMapping("/medicine/{medicineId}")
    public List<CategoryDTO> getCategoriesByMedicine(@PathVariable Long medicineId) {
        return categoryService.getCategoriesByMedicineId(medicineId).stream()
                .map(DTOMapper::toCategoryDTOSimple)
                .collect(Collectors.toList());
    }

    @GetMapping("/with-medicines")
    public List<CategoryDTO> getCategoriesWithMedicines() {
        return categoryService.getCategoriesWithMedicines().stream()
                .map(DTOMapper::toCategoryDTO)
                .collect(Collectors.toList());
    }

    @PutMapping("/{id}")
    public ResponseEntity<CategoryDTO> updateCategory(@PathVariable Long id, @RequestBody CategoryUpdateRequest request) {
        try {
            Category category = categoryService.updateCategory(id, request.getName(), request.getDescription());
            CategoryDTO categoryDTO = DTOMapper.toCategoryDTOSimple(category);
            return new ResponseEntity<>(categoryDTO, HttpStatus.OK);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    @PatchMapping("/{id}/deactivate")
    public ResponseEntity<CategoryDTO> deactivateCategory(@PathVariable Long id) {
        try {
            Category category = categoryService.deactivateCategory(id);
            CategoryDTO categoryDTO = DTOMapper.toCategoryDTOSimple(category);
            return new ResponseEntity<>(categoryDTO, HttpStatus.OK);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @PatchMapping("/{id}/activate")
    public ResponseEntity<CategoryDTO> activateCategory(@PathVariable Long id) {
        try {
            Category category = categoryService.activateCategory(id);
            CategoryDTO categoryDTO = DTOMapper.toCategoryDTOSimple(category);
            return new ResponseEntity<>(categoryDTO, HttpStatus.OK);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @PostMapping("/{categoryId}/medicines/{medicineId}")
    public ResponseEntity<CategoryDTO> addMedicineToCategory(
            @PathVariable Long categoryId,
            @PathVariable Long medicineId) {
        try {
            Category category = categoryService.addMedicineToCategory(categoryId, medicineId);
            CategoryDTO categoryDTO = DTOMapper.toCategoryDTO(category);
            return new ResponseEntity<>(categoryDTO, HttpStatus.OK);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    @DeleteMapping("/{categoryId}/medicines/{medicineId}")
    public ResponseEntity<CategoryDTO> removeMedicineFromCategory(
            @PathVariable Long categoryId,
            @PathVariable Long medicineId) {
        try {
            Category category = categoryService.removeMedicineFromCategory(categoryId, medicineId);
            CategoryDTO categoryDTO = DTOMapper.toCategoryDTO(category);
            return new ResponseEntity<>(categoryDTO, HttpStatus.OK);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    public static class CategoryCreateRequest {
        private String name;
        private String description;

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
    }

    public static class CategoryUpdateRequest {
        private String name;
        private String description;

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
    }
}