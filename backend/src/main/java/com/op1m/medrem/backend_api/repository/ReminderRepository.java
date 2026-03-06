package com.op1m.medrem.backend_api.repository;

import com.op1m.medrem.backend_api.entity.User;
import com.op1m.medrem.backend_api.entity.Reminder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ReminderRepository extends JpaRepository<Reminder, Long> {

    List<Reminder> findByUser(User user);

    List<Reminder> findByUserAndIsActiveTrue(User user);

    List<Reminder> findByIsActiveTrue();
    @Query("select r from Reminder r " +
            "left join fetch r.medicine m " +
            "left join fetch r.user u " +
            "where r.isActive = true")
    List<Reminder> findAllActiveWithUserAndMedicine();
    @Query("select r from Reminder r " +
            "left join fetch r.medicine m " +
            "left join fetch r.user u " +
            "where r.user.id = :userId")
    List<Reminder> findByUserIdWithMedicine(@Param("userId") Long userId);

    @Query("SELECT r FROM Reminder r " +
            "LEFT JOIN FETCH r.user u " +
            "LEFT JOIN FETCH r.medicine m " +
            "WHERE r.id = :id")
    Optional<Reminder> findByIdWithUserAndMedicine(@Param("id") Long id);
}
