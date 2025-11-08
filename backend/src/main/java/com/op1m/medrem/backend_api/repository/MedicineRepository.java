package com.op1m.medrem.backend_api.repository;

import org.springframework.stereotype.Repository;
import com.op1m.medrem.backend_api.entity.Medicine;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface MedicineRepository extends JpaRepository<Medicine, Long>{
    List<Medicine> findByIsActiveTrue();
    List<Medicine> findByNameContainingIgnoreCaseAndIsActiveTrue(String name);
    List<Medicine> findByNameContainingIgnoreCase(String name);
    boolean existsByNameAndIsActiveTrue(String name);
}
