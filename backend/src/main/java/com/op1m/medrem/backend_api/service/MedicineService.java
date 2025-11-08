package com.op1m.medrem.backend_api.service;

import com.op1m.medrem.backend_api.entity.Medicine;
import java.util.List;

public interface MedicineService {
    Medicine createMedicine(String name, String dosage, String description, String instructions);
    Medicine findById(Long id);
    List<Medicine> getAllActiveMedicines();
    List<Medicine> searchMedicines(String name);
    Medicine deactivateMedicine(Long id);
    Medicine updateMedicine(Long id, String name, String dosage, String description, String instructions);
}
