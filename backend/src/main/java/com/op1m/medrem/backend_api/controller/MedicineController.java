package com.op1m.medrem.backend_api.controller;

import com.op1m.medrem.backend_api.entity.Medicine;
import com.op1m.medrem.backend_api.service.MedicineService;
import com.op1m.medrem.backend_api.dto.MedicineDTO;
import com.op1m.medrem.backend_api.dto.DTOMapper;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.beans.factory.annotation.Autowired;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;


@RestController
@RequestMapping("/api/medicines")
public class MedicineController {
    @Autowired
    private MedicineService medicineService;

    @PostMapping
    public ResponseEntity<MedicineDTO> createMedicine(@Valid @RequestBody MedicineCreateRequest request) {
        Medicine medicine = medicineService.createMedicine(
                request.getName(),
                request.getDosage(),
                request.getDescription(),
                request.getInstructions()
        );

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        System.out.println("👤 Текущий пользователь: " + auth.getName());
        System.out.println("🔐 Authorities: " + auth.getAuthorities());

        MedicineDTO medicineDTO = DTOMapper.toMedicineDTO(medicine);
        return new ResponseEntity<>(medicineDTO, HttpStatus.CREATED);
    }

    @GetMapping
    public List<MedicineDTO> getAllActiveMedicines() {
        return medicineService.getAllActiveMedicines().stream().map(DTOMapper::toMedicineDTO).collect(Collectors.toList());
    }

    @GetMapping("/search")
    public List<MedicineDTO> searchMedicines(@RequestParam String name) {
        return medicineService.searchMedicines(name).stream().map(DTOMapper::toMedicineDTO).collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    public ResponseEntity<MedicineDTO> getMedicineById(@PathVariable Long id) {
        Medicine medicine = medicineService.findById(id);
        MedicineDTO medicineDTO = DTOMapper.toMedicineDTO(medicine);
        if(medicine != null) {
            return new ResponseEntity<>(medicineDTO, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deactivateMedicine(@PathVariable Long id) {
        Medicine medicine = medicineService.deactivateMedicine(id);
        if(medicine != null) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<MedicineDTO> updateMedicine (@PathVariable Long id, @Valid @RequestBody MedicineUpdateRequest request) {
        Medicine updatedMedicine = medicineService.updateMedicine( id,
                request.getName(),
                request.getDosage(),
                request.getDescription(),
                request.getInstructions()
        );
        MedicineDTO medicineDTO = DTOMapper.toMedicineDTO(updatedMedicine);
        if(updatedMedicine != null) {
            return new ResponseEntity<>(medicineDTO, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    public static class MedicineCreateRequest {
        private String name;
        private String dosage;
        private String description;
        private String instructions;

        public String getName() {return  name;}
        public void setName(String name) {this.name = name;}

        public String getDosage() {return  dosage;}
        public void setDosage(String dosage) {this.dosage = dosage;}

        public String getDescription() {return  description;}
        public void setDescription(String description) {this.description = description;}

        public String getInstructions() {return  instructions;}
        public void setInstructions(String instructions) {this.instructions = instructions;}
    }

    public static class MedicineUpdateRequest {
        @NotBlank(message = "Название лекарства обязательно")
        @Size(min = 2, max = 100, message = "Название должно быть от 2 до 100 символов")
        private String name;

        @NotBlank(message = "Дозировка обязательна")
        @Size(max = 50, message = "Дозировка не более 50 символов")
        private String dosage;

        @Size(max = 500, message = "Описание не более 500 символов")
        private String description;

        @Size(max = 500, message = "Инструкции не более 500 символов")
        private String instructions;

        public String getName() {return  name;}
        public void setName(String name) {this.name = name;}

        public String getDosage() {return  dosage;}
        public void setDosage(String dosage) {this.dosage = dosage;}

        public String getDescription() {return  description;}
        public void setDescription(String description) {this.description = description;}

        public String getInstructions() {return  instructions;}
        public void setInstructions(String name) {this.instructions = instructions;}
    }
}
