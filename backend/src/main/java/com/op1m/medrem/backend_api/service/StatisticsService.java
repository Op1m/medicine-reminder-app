package com.op1m.medrem.backend_api.service;

import com.op1m.medrem.backend_api.dto.StatisticsDTO;

import java.time.LocalDate;

public interface StatisticsService {
    StatisticsDTO.ComplianceStats getComplianceStats(Long userId, LocalDate startDate, LocalDate endDate);
    StatisticsDTO.MedicationStats getMedicationStats(Long userId, LocalDate startDate, LocalDate endDate);
    StatisticsDTO.OverviewStats getOverviewStats(Long userId);
    StatisticsDTO.PeriodStats getPeriodStats(Long userId, LocalDate startDate, LocalDate endDate);
}