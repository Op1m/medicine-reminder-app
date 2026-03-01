package com.op1m.medrem.backend_api.service.impl;

import com.op1m.medrem.backend_api.entity.Category;
import com.op1m.medrem.backend_api.entity.Medicine;
import com.op1m.medrem.backend_api.repository.CategoryRepository;
import com.op1m.medrem.backend_api.service.CategoryService;
import com.op1m.medrem.backend_api.service.MedicineService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CategoryServiceImpl implements CategoryService {

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private MedicineService medicineService;

    @Override
    public Category createCategory(String name, String description) {
        System.out.println("🏷️ CategoryService: Создание категории: " + name);

        if (categoryRepository.existsByName(name)) {
            throw new RuntimeException("❌ CategoryService: Категория с названием '" + name + "' уже существует");
        }

        Category category = new Category(name, description);
        Category savedCategory = categoryRepository.save(category);

        System.out.println("✅ CategoryService: Категория создана: " + savedCategory.getId());
        return savedCategory;
    }

    @Override
    public Category findById(Long id) {
        System.out.println("🔍 CategoryService: Поиск категории по ID: " + id);

        return categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("❌ CategoryService: Категория с ID " + id + " не найдена"));
    }

    @Override
    public List<Category> getAllActiveCategories() {
        System.out.println("📋 CategoryService: Получение всех активных категорий");

        List<Category> categories = categoryRepository.findByIsActiveTrue();
        System.out.println("✅ CategoryService: Найдено активных категорий: " + categories.size());

        return categories;
    }

    @Override
    public List<Category> searchCategories(String name) {
        System.out.println("🔍 CategoryService: Поиск категорий по названию: " + name);

        List<Category> categories = categoryRepository.findByNameContainingIgnoreCaseAndIsActiveTrue(name);
        System.out.println("✅ CategoryService: Найдено категорий: " + categories.size());

        return categories;
    }

    @Override
    public Category updateCategory(Long id, String name, String description) {
        System.out.println("✏️ CategoryService: Обновление категории: " + id);

        Category category = findById(id);

        if (!category.getName().equals(name) && categoryRepository.existsByName(name)) {
            throw new RuntimeException("❌ CategoryService: Категория с названием '" + name + "' уже существует");
        }

        category.setName(name);
        category.setDescription(description);

        Category updatedCategory = categoryRepository.save(category);
        System.out.println("✅ CategoryService: Категория обновлена: " + updatedCategory.getId());

        return updatedCategory;
    }

    @Override
    public Category deactivateCategory(Long id) {
        System.out.println("🚫 CategoryService: Деактивация категории: " + id);

        Category category = findById(id);
        category.deactivate();

        Category deactivatedCategory = categoryRepository.save(category);
        System.out.println("✅ CategoryService: Категория деактивирована: " + deactivatedCategory.getId());

        return deactivatedCategory;
    }

    @Override
    public Category activateCategory(Long id) {
        System.out.println("✅ CategoryService: Активация категории: " + id);

        Category category = findById(id);
        category.activate();

        Category activatedCategory = categoryRepository.save(category);
        System.out.println("✅ CategoryService: Категория активирована: " + activatedCategory.getId());

        return activatedCategory;
    }

    @Override
    public List<Category> getCategoriesByMedicineId(Long medicineId) {
        System.out.println("🔍 CategoryService: Получение категорий для лекарства: " + medicineId);

        List<Category> categories = categoryRepository.findByMedicineId(medicineId);
        System.out.println("✅ CategoryService: Найдено категорий для лекарства: " + categories.size());

        return categories;
    }

    @Override
    @Transactional
    public Category addMedicineToCategory(Long categoryId, Long medicineId) {
        System.out.println("➕ CategoryService: Добавление лекарства " + medicineId + " в категорию " + categoryId);

        Category category = findById(categoryId);
        Medicine medicine = medicineService.findById(medicineId);

        category.getMedicines().add(medicine);
        medicine.getCategories().add(category);

        Category updatedCategory = categoryRepository.save(category);
        System.out.println("✅ CategoryService: Лекарство добавлено в категорию");

        return updatedCategory;
    }

    @Override
    @Transactional
    public Category removeMedicineFromCategory(Long categoryId, Long medicineId) {
        System.out.println("➖ CategoryService: Удаление лекарства " + medicineId + " из категории " + categoryId);

        Category category = findById(categoryId);
        Medicine medicine = medicineService.findById(medicineId);

        category.getMedicines().remove(medicine);
        medicine.getCategories().remove(category);

        Category updatedCategory = categoryRepository.save(category);
        System.out.println("✅ CategoryService: Лекарство удалено из категории");

        return updatedCategory;
    }

    @Override
    public List<Category> getCategoriesWithMedicines() {
        System.out.println("📋 CategoryService: Получение категорий с лекарствами");

        List<Category> categories = categoryRepository.findCategoriesWithMedicines();
        System.out.println("✅ CategoryService: Найдено категорий с лекарствами: " + categories.size());

        return categories;
    }
}