package com.op1m.medrem.backend_api.repository;

import org.springframework.stereotype.Repository;
import com.op1m.medrem.backend_api.entity.User;
import com.op1m.medrem.backend_api.entity.Reminder;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ReminderRepository extends JpaRepository<Reminder, Long>{
    List<Reminder> findByUser(User user);
    List<Reminder> findByUserAndIsActiveTrue (User user);
    List<Reminder> findByIsActiveTrue ();
}
