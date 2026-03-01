package com.op1m.medrem.backend_api.repository;

import com.op1m.medrem.backend_api.entity.MedicineHistory;
import com.op1m.medrem.backend_api.entity.User;
import com.op1m.medrem.backend_api.entity.enums.MedicineStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

public interface MedicineHistoryRepository extends JpaRepository<MedicineHistory, Long> {
    List<MedicineHistory> findByReminderUserOrderByScheduledTimeDesc(User user);
    List<MedicineHistory> findByReminderUserAndStatusOrderByScheduledTimeDesc(User user, MedicineStatus status);
    @Query("SELECT mh FROM MedicineHistory mh WHERE mh.reminder.user = :user AND mh.scheduledTime BETWEEN :start AND :end ORDER BY mh.scheduledTime DESC")
    List<MedicineHistory> findByUserAndPeriod(@Param("user") User user,
                                              @Param("start") LocalDateTime start,
                                              @Param("end") LocalDateTime end);
    List<MedicineHistory> findByStatusAndScheduledTimeBefore(MedicineStatus status, LocalDateTime scheduledTime);
    @Modifying
    @Transactional
    @Query("delete from MedicineHistory mh where mh.reminder.id = :reminderId")
    void deleteByReminderId(@Param("reminderId") Long reminderId);
}
