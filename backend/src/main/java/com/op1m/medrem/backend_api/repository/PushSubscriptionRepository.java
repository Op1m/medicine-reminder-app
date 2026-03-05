package com.op1m.medrem.backend_api.repository;

import com.op1m.medrem.backend_api.entity.PushSubscription;
import com.op1m.medrem.backend_api.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PushSubscriptionRepository extends JpaRepository<PushSubscription, Long> {
    List<PushSubscription> findByUser(User user);
    Optional<PushSubscription> findByUserAndEndpoint(User user, String endpoint);
    void deleteByUserAndEndpoint(User user, String endpoint);
}