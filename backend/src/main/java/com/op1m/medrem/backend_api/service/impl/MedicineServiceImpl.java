package com.op1m.medrem.backend_api.service.impl;

import com.op1m.medrem.backend_api.entity.Medicine;
import com.op1m.medrem.backend_api.repository.MedicineRepository;
import com.op1m.medrem.backend_api.service.MedicineService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class MedicineServiceImpl implements MedicineService{

    private final MedicineRepository medicineRepository;

    @Autowired
    public MedicineServiceImpl(MedicineRepository medicineRepository) {
        this.medicineRepository = medicineRepository;
    }

    @Override
    public Medicine createMedicine(String name, String dosage, String description, String instructions) {
        System.out.println("💊 MedicineService: Создание лекарства: " + name);

        Medicine medicine = new Medicine(name, dosage);
        medicine.setDescription(description);
        medicine.setInstructions(instructions);

        Medicine savedMedicine = medicineRepository.save(medicine);
        System.out.println("✅ MedicineService: Лекарство создано: " + savedMedicine.getId());

        return savedMedicine;
    }

    @Override
    public Medicine findById(Long id) {
        System.out.println("🔍 MedicineService: Поиск лекарства по ID: " + id);

        return medicineRepository.findById(id).orElse(null);
    }

    @Override
    public List<Medicine> getAllActiveMedicines() {
        System.out.println("📋 MedicineService: Получение всех активных лекарств");

        List<Medicine> medicines = medicineRepository.findByIsActiveTrue();
        System.out.println("✅ MedicineService: Найдено активных лекарств: " + medicines.size());
        
        return medicines;
    }

    @Override
    public List<Medicine> searchMedicines(String name) {
        System.out.println("🔍 MedicineService: Поиск лекарств по названию: " + name);

        List<Medicine> medicines = medicineRepository.findByNameContainingIgnoreCaseAndIsActiveTrue(name);
        System.out.println("✅ MedicineService: Найдено лекарств: " + medicines.size());

        return medicines;
    }

    @Override
    public Medicine deactivateMedicine(Long id) {
        System.out.println("🚫 MedicineService: Деактивация лекарства: " + id);

        Medicine medicine = findById(id);

        if (medicine != null) {
            medicine.deactivate();
            medicine.setActive(false);
            Medicine updatedMedicine = medicineRepository.save(medicine);
            System.out.println("✅ MedicineService: Лекарство деактивировано: " + updatedMedicine.getId());

            return updatedMedicine;
        }

        System.out.println("❌ MedicineService: Лекарство не найдено для деактивации: " + id);
        return null;
    }

    @Override
    public Medicine updateMedicine(Long id, String name, String dosage, String description, String instructions) {
        System.out.println("✏️ MedicineService: Обновление лекарства: " + id);

        Medicine medicine = findById(id);
        if (medicine == null) {
            System.out.println("❌ MedicineService: Лекарство не найдено: " + id);
            return null;
        }

        medicine.setName(name);
        medicine.setDosage(dosage);
        medicine.setDescription(description);
        medicine.setInstructions(instructions);

        Medicine updatedMedicine = medicineRepository.save(medicine);
        System.out.println("✅ MedicineService: Лекарство обновлено: " + updatedMedicine.getId());
        return updatedMedicine;
    }
}
