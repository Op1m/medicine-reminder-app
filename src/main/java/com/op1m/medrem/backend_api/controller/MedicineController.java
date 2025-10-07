package com.op1m.medrem.backend_api.controller;

import com.op1m.medrem.backend_api.entity.Medicine;
import com.op1m.medrem.backend_api.service.MedicineService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/api/medicines")
public class MedicineController {
    @Autowired
    private MedicineService medicineService;

    @PostMapping
    public ResponseEntity<Medicine> createMedicine(@RequestBody MedicineCreateRequest request) {
        Medicine medicine = medicineService.createMedicine(
                request.getName(),
                request.getDosage(),
                request.getDescription(),
                request.getInstructions()
        );

        return new ResponseEntity<>(medicine, HttpStatus.CREATED);
    }

    @GetMapping
    public List<Medicine> getAllActiveMedicines() {
        return medicineService.getAllActiveMedicines();
    }

    @GetMapping("/search")
    public List<Medicine> searchMedicines(@RequestParam String name) {
        return medicineService.searchMedicines(name);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Medicine> getMedicineById(@PathVariable Long id) {
        Medicine medicine = medicineService.findById(id);
        if(medicine != null) {
            return new ResponseEntity<>(medicine, HttpStatus.OK);
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
    public ResponseEntity<Medicine> updateMedicine (@PathVariable Long id, @RequestBody MedicineUpdateRequest request) {
        Medicine updatedMedicine = medicineService.updateMedicine( id,
                request.getName(),
                request.getDosage(),
                request.getDescription(),
                request.getInstructions()
        );

        if(updatedMedicine != null) {
            return new ResponseEntity<>(updatedMedicine, HttpStatus.OK);
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
        public void setInstructions(String name) {this.instructions = instructions;}
    }

    public static class MedicineUpdateRequest {
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
        public void setInstructions(String name) {this.instructions = instructions;}
    }
}
