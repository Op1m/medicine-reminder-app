package com.op1m.medrem.backend_api.service.impl;

import com.op1m.medrem.backend_api.dto.StatisticsDTO;
import com.op1m.medrem.backend_api.entity.MedicineHistory;
import com.op1m.medrem.backend_api.entity.enums.MedicineStatus;
import com.op1m.medrem.backend_api.entity.Reminder;
import com.op1m.medrem.backend_api.entity.User;
import com.op1m.medrem.backend_api.repository.MedicineHistoryRepository;
import com.op1m.medrem.backend_api.repository.ReminderRepository;
import com.op1m.medrem.backend_api.service.StatisticsService;
import com.op1m.medrem.backend_api.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class StatisticsServiceImpl implements StatisticsService {

    @Autowired
    private UserService userService;

    @Autowired
    private MedicineHistoryRepository medicineHistoryRepository;

    @Autowired
    private ReminderRepository reminderRepository;

    @Override
    public StatisticsDTO.ComplianceStats getComplianceStats(Long userId, LocalDate startDate, LocalDate endDate) {
        System.out.println("📊 StatisticsService: Получение статистики соблюдения для пользователя: " + userId);

        User user = userService.findById(userId);
        if (user == null) {
            throw new RuntimeException("User not found");
        }

        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(23, 59, 59);

        List<MedicineHistory> history = medicineHistoryRepository.findByUserAndPeriod(user, startDateTime, endDateTime);

        int total = history.size();
        int taken = (int) history.stream().filter(h -> h.getStatus() == MedicineStatus.TAKEN).count();
        int skipped = (int) history.stream().filter(h -> h.getStatus() == MedicineStatus.SKIPPED).count();
        int missed = (int) history.stream().filter(h -> h.getStatus() == MedicineStatus.MISSED).count();

        StatisticsDTO.ComplianceStats stats = new StatisticsDTO.ComplianceStats(total, taken, skipped, missed);
        System.out.println("✅ StatisticsService: Статистика соблюдения: " + stats.getComplianceRate() + "%");

        return stats;
    }

    @Override
    public StatisticsDTO.MedicationStats getMedicationStats(Long userId, LocalDate startDate, LocalDate endDate) {
        System.out.println("📊 StatisticsService: Получение статистики по лекарствам для пользователя: " + userId);

        User user = userService.findById(userId);
        if (user == null) {
            throw new RuntimeException("User not found");
        }

        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(23, 59, 59);

        List<MedicineHistory> history = medicineHistoryRepository.findByUserAndPeriod(user, startDateTime, endDateTime);

        StatisticsDTO.MedicationStats stats = new StatisticsDTO.MedicationStats();

        Map<String, Integer> frequency = history.stream()
                .collect(Collectors.groupingBy(
                        h -> h.getReminder().getMedicine().getName(),
                        Collectors.summingInt(h -> 1)
                ));
        stats.setMedicineFrequency(frequency);

        frequency.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .ifPresent(entry -> stats.setMostFrequentMedicine(entry.getKey()));

        Map<String, Long> missedByMedicine = history.stream()
                .filter(h -> h.getStatus() == MedicineStatus.MISSED || h.getStatus() == MedicineStatus.SKIPPED)
                .collect(Collectors.groupingBy(
                        h -> h.getReminder().getMedicine().getName(),
                        Collectors.counting()
                ));

        if (!missedByMedicine.isEmpty()) {
            missedByMedicine.entrySet().stream()
                    .max(Map.Entry.comparingByValue())
                    .ifPresent(entry -> stats.setMostMissedMedicine(entry.getKey()));
        }

        Map<String, Double> complianceByMedicine = new HashMap<>();
        for (String medicineName : frequency.keySet()) {
            List<MedicineHistory> medicineHistory = history.stream()
                    .filter(h -> h.getReminder().getMedicine().getName().equals(medicineName))
                    .collect(Collectors.toList());

            long takenCount = medicineHistory.stream()
                    .filter(h -> h.getStatus() == MedicineStatus.TAKEN)
                    .count();

            double compliance = medicineHistory.isEmpty() ? 0 : (double) takenCount / medicineHistory.size() * 100;
            complianceByMedicine.put(medicineName, Math.round(compliance * 100.0) / 100.0);
        }
        stats.setMedicineCompliance(complianceByMedicine);

        System.out.println("✅ StatisticsService: Статистика по лекарствам готова");
        return stats;
    }

    @Override
    public StatisticsDTO.OverviewStats getOverviewStats(Long userId) {
        System.out.println("📊 StatisticsService: Получение общей статистики для пользователя: " + userId);

        User user = userService.findById(userId);
        if (user == null) {
            throw new RuntimeException("User not found");
        }

        StatisticsDTO.OverviewStats stats = new StatisticsDTO.OverviewStats();

        List<Reminder> activeReminders = reminderRepository.findByUserAndIsActiveTrue(user);
        stats.setActiveReminders(activeReminders.size());

        long uniqueMedicines = activeReminders.stream()
                .map(r -> r.getMedicine().getId())
                .distinct()
                .count();
        stats.setTotalMedicines((int) uniqueMedicines);

        List<MedicineHistory> allHistory = medicineHistoryRepository.findByReminderUserOrderByScheduledTimeDesc(user);
        Optional<MedicineHistory> lastTaken = allHistory.stream()
                .filter(h -> h.getStatus() == MedicineStatus.TAKEN)
                .findFirst();

        lastTaken.ifPresent(history ->
                stats.setLastTaken(history.getTakenAt().toLocalDate())
        );

        stats.setStreakDays(calculateStreakDays(user));

        LocalDate today = LocalDate.now();
        long todayRemindersCount = allHistory.stream()
                .filter(h -> h.getScheduledTime().toLocalDate().equals(today))
                .count();
        stats.setTodayReminders((int) todayRemindersCount);

        long pendingToday = allHistory.stream()
                .filter(h -> h.getScheduledTime().toLocalDate().equals(today))
                .filter(h -> h.getStatus() == MedicineStatus.PENDING)
                .count();
        stats.setPendingToday((int) pendingToday);

        System.out.println("✅ StatisticsService: Общая статистика готова");
        return stats;
    }

    @Override
    public StatisticsDTO.PeriodStats getPeriodStats(Long userId, LocalDate startDate, LocalDate endDate) {
        System.out.println("📊 StatisticsService: Получение статистики за период для пользователя: " + userId);

        StatisticsDTO.PeriodStats periodStats = new StatisticsDTO.PeriodStats();
        periodStats.setStartDate(startDate);
        periodStats.setEndDate(endDate);
        periodStats.setCompliance(getComplianceStats(userId, startDate, endDate));
        periodStats.setMedications(getMedicationStats(userId, startDate, endDate));

        System.out.println("✅ StatisticsService: Статистика за период готова");
        return periodStats;
    }

    private int calculateStreakDays(User user) {
        LocalDate currentDate = LocalDate.now();
        int streak = 0;

        for (int i = 0; i < 30; i++) {
            LocalDate checkDate = currentDate.minusDays(i);
            LocalDateTime startOfDay = checkDate.atStartOfDay();
            LocalDateTime endOfDay = checkDate.atTime(23, 59, 59);

            List<MedicineHistory> dayHistory = medicineHistoryRepository.findByUserAndPeriod(user, startOfDay, endOfDay);

            if (dayHistory.isEmpty()) {
                continue;
            }

            boolean allTaken = dayHistory.stream()
                    .allMatch(h -> h.getStatus() == MedicineStatus.TAKEN);

            if (allTaken) {
                streak++;
            } else {
                break;
            }
        }

        return streak;
    }
}