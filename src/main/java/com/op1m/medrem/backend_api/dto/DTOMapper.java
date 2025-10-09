package com.op1m.medrem.backend_api.dto;

import com.op1m.medrem.backend_api.entity.MedicineHistory;
import com.op1m.medrem.backend_api.entity.User;
import com.op1m.medrem.backend_api.entity.Medicine;
import com.op1m.medrem.backend_api.entity.Reminder;
import org.springframework.stereotype.Component;

@Component
public class DTOMapper {
    public static UserDTO toUserDTO(User user) {
        if (user == null) return null;

        return new UserDTO(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getFirstName(),
                user.getLastName(),
                user.getTelegramChatId(),
                user.getCreatedAt(),
                user.getUpdatedAt()
        );

    }

    public static MedicineDTO toMedicineDTO(Medicine medicine) {
        if (medicine == null) return null;

        return new MedicineDTO(
                medicine.getId(),
                medicine.getName(),
                medicine.getDosage(),
                medicine.getDescription(),
                medicine.getInstructions(),
                medicine.isActive(),
                medicine.getCreatedAt(),
                medicine.getUpdatedAt()
        );
    }

    public static ReminderDTO toReminderDTO(Reminder reminder) {
        if (reminder == null) return null;

        return new ReminderDTO(
                reminder.getId(),
                toUserDTO(reminder.getUser()),
                toMedicineDTO(reminder.getMedicine()),
                reminder.getReminderTime(),
                reminder.getIsActive(),
                reminder.getDaysOfWeek(),
                reminder.getCreatedAt(),
                reminder.getUpdatedAt()
        );
    }

    public static MedicineHistoryDTO toMedicineHistoryDTO(MedicineHistory history) {
        if (history == null) return null;

        return new MedicineHistoryDTO(
                history.getId(),
                toReminderDTO(history.getReminder()),
                history.getScheduledTime(),
                history.getTakenAt(),
                history.getStatus(),
                history.getNotes(),
                history.getCreatedAt()
        );
    }
}
