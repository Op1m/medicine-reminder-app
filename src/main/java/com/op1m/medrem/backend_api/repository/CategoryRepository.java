package com.op1m.medrem.backend_api.repository;

import com.op1m.medrem.backend_api.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CategoryRepository extends JpaRepository<Category, Long> {

    List<Category> findByIsActiveTrue();

    Optional<Category> findByName(String name);

    boolean existsByName(String name);

    List<Category> findByNameContainingIgnoreCaseAndIsActiveTrue(String name);

    @Query("SELECT c FROM Category c JOIN c.medicines m WHERE m.id = :medicineId AND c.isActive = true")
    List<Category> findByMedicineId(@Param("medicineId") Long medicineId);

    @Query("SELECT c FROM Category c WHERE SIZE(c.medicines) > 0 AND c.isActive = true")
    List<Category> findCategoriesWithMedicines();
}