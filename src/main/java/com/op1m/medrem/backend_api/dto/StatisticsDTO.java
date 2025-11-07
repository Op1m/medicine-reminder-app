package com.op1m.medrem.backend_api.dto;

import java.time.LocalDate;
import java.util.Map;

public class StatisticsDTO {

    public static class ComplianceStats {
        private int totalScheduled;
        private int taken;
        private int skipped;
        private int missed;
        private double complianceRate;

        public ComplianceStats() {}

        public ComplianceStats(int totalScheduled, int taken, int skipped, int missed) {
            this.totalScheduled = totalScheduled;
            this.taken = taken;
            this.skipped = skipped;
            this.missed = missed;
            this.complianceRate = totalScheduled > 0 ? (double) taken / totalScheduled * 100 : 0;
        }

        public int getTotalScheduled() { return totalScheduled; }
        public void setTotalScheduled(int totalScheduled) { this.totalScheduled = totalScheduled; }

        public int getTaken() { return taken; }
        public void setTaken(int taken) { this.taken = taken; }

        public int getSkipped() { return skipped; }
        public void setSkipped(int skipped) { this.skipped = skipped; }

        public int getMissed() { return missed; }
        public void setMissed(int missed) { this.missed = missed; }

        public double getComplianceRate() { return complianceRate; }
        public void setComplianceRate(double complianceRate) { this.complianceRate = complianceRate; }
    }

    public static class MedicationStats {
        private String mostFrequentMedicine;
        private String mostMissedMedicine;
        private Map<String, Integer> medicineFrequency;
        private Map<String, Double> medicineCompliance;

        public MedicationStats() {}

        public String getMostFrequentMedicine() { return mostFrequentMedicine; }
        public void setMostFrequentMedicine(String mostFrequentMedicine) { this.mostFrequentMedicine = mostFrequentMedicine; }

        public String getMostMissedMedicine() { return mostMissedMedicine; }
        public void setMostMissedMedicine(String mostMissedMedicine) { this.mostMissedMedicine = mostMissedMedicine; }

        public Map<String, Integer> getMedicineFrequency() { return medicineFrequency; }
        public void setMedicineFrequency(Map<String, Integer> medicineFrequency) { this.medicineFrequency = medicineFrequency; }

        public Map<String, Double> getMedicineCompliance() { return medicineCompliance; }
        public void setMedicineCompliance(Map<String, Double> medicineCompliance) { this.medicineCompliance = medicineCompliance; }
    }

    public static class OverviewStats {
        private int activeReminders;
        private int totalMedicines;
        private LocalDate lastTaken;
        private int streakDays;
        private int todayReminders;
        private int pendingToday;

        public OverviewStats() {}

        public int getActiveReminders() { return activeReminders; }
        public void setActiveReminders(int activeReminders) { this.activeReminders = activeReminders; }

        public int getTotalMedicines() { return totalMedicines; }
        public void setTotalMedicines(int totalMedicines) { this.totalMedicines = totalMedicines; }

        public LocalDate getLastTaken() { return lastTaken; }
        public void setLastTaken(LocalDate lastTaken) { this.lastTaken = lastTaken; }

        public int getStreakDays() { return streakDays; }
        public void setStreakDays(int streakDays) { this.streakDays = streakDays; }

        public int getTodayReminders() { return todayReminders; }
        public void setTodayReminders(int todayReminders) { this.todayReminders = todayReminders; }

        public int getPendingToday() { return pendingToday; }
        public void setPendingToday(int pendingToday) { this.pendingToday = pendingToday; }
    }

    public static class PeriodStats {
        private LocalDate startDate;
        private LocalDate endDate;
        private ComplianceStats compliance;
        private MedicationStats medications;

        public PeriodStats() {}

        public LocalDate getStartDate() { return startDate; }
        public void setStartDate(LocalDate startDate) { this.startDate = startDate; }

        public LocalDate getEndDate() { return endDate; }
        public void setEndDate(LocalDate endDate) { this.endDate = endDate; }

        public ComplianceStats getCompliance() { return compliance; }
        public void setCompliance(ComplianceStats compliance) { this.compliance = compliance; }

        public MedicationStats getMedications() { return medications; }
        public void setMedications(MedicationStats medications) { this.medications = medications; }
    }
}