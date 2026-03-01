package com.op1m.medrem.backend_api.controller;

import com.op1m.medrem.backend_api.dto.StatisticsDTO;
import com.op1m.medrem.backend_api.service.StatisticsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/statistics")
public class StatisticsController {

    @Autowired
    private StatisticsService statisticsService;

    @GetMapping("/user/{userId}/compliance")
    public ResponseEntity<StatisticsDTO.ComplianceStats> getComplianceStats(
            @PathVariable Long userId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        try {
            System.out.println("📊 StatisticsController: Запрос статистики соблюдения для пользователя " + userId);
            StatisticsDTO.ComplianceStats stats = statisticsService.getComplianceStats(userId, startDate, endDate);
            return new ResponseEntity<>(stats, HttpStatus.OK);
        } catch (RuntimeException e) {
            System.out.println("❌ StatisticsController: Ошибка - " + e.getMessage());
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping("/user/{userId}/medications")
    public ResponseEntity<StatisticsDTO.MedicationStats> getMedicationStats(
            @PathVariable Long userId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        try {
            System.out.println("📊 StatisticsController: Запрос статистики по лекарствам для пользователя " + userId);
            StatisticsDTO.MedicationStats stats = statisticsService.getMedicationStats(userId, startDate, endDate);
            return new ResponseEntity<>(stats, HttpStatus.OK);
        } catch (RuntimeException e) {
            System.out.println("❌ StatisticsController: Ошибка - " + e.getMessage());
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping("/user/{userId}/overview")
    public ResponseEntity<StatisticsDTO.OverviewStats> getOverviewStats(@PathVariable Long userId) {
        try {
            System.out.println("📊 StatisticsController: Запрос общей статистики для пользователя " + userId);
            StatisticsDTO.OverviewStats stats = statisticsService.getOverviewStats(userId);
            return new ResponseEntity<>(stats, HttpStatus.OK);
        } catch (RuntimeException e) {
            System.out.println("❌ StatisticsController: Ошибка - " + e.getMessage());
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping("/user/{userId}/period")
    public ResponseEntity<StatisticsDTO.PeriodStats> getPeriodStats(
            @PathVariable Long userId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        try {
            System.out.println("📊 StatisticsController: Запрос статистики за период для пользователя " + userId);
            StatisticsDTO.PeriodStats stats = statisticsService.getPeriodStats(userId, startDate, endDate);
            return new ResponseEntity<>(stats, HttpStatus.OK);
        } catch (RuntimeException e) {
            System.out.println("❌ StatisticsController: Ошибка - " + e.getMessage());
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping("/user/{userId}/streak")
    public ResponseEntity<Integer> getCurrentStreak(@PathVariable Long userId) {
        try {
            StatisticsDTO.OverviewStats overview = statisticsService.getOverviewStats(userId);
            return new ResponseEntity<>(overview.getStreakDays(), HttpStatus.OK);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }
}