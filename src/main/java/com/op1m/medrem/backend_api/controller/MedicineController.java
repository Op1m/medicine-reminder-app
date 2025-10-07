package com.op1m.medrem.backend_api.controller;

import com.op1m.medrem.backend_api.entity.Medicine;
import com.op1m.medrem.backend_api.service.MedicineService;
import com.op1m.medrem.backend_api.dto.MedicineDTO;
import com.op1m.medrem.backend_api.dto.DTOMapper;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.beans.factory.annotation.Autowired;
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
    public ResponseEntity<MedicineDTO> createMedicine(@RequestBody MedicineCreateRequest request) {
        Medicine medicine = medicineService.createMedicine(
                request.getName(),
                request.getDosage(),
                request.getDescription(),
                request.getInstructions()
        );
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
    public ResponseEntity<MedicineDTO> updateMedicine (@PathVariable Long id, @RequestBody MedicineUpdateRequest request) {
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
