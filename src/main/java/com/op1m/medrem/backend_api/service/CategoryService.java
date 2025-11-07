package com.op1m.medrem.backend_api.service;

import com.op1m.medrem.backend_api.entity.Category;
import java.util.List;

public interface CategoryService {
    Category createCategory(String name, String description);
    Category findById(Long id);
    List<Category> getAllActiveCategories();
    List<Category> searchCategories(String name);
    Category updateCategory(Long id, String name, String description);
    Category deactivateCategory(Long id);
    Category activateCategory(Long id);
    List<Category> getCategoriesByMedicineId(Long medicineId);
    Category addMedicineToCategory(Long categoryId, Long medicineId);
    Category removeMedicineFromCategory(Long categoryId, Long medicineId);
    List<Category> getCategoriesWithMedicines();
}