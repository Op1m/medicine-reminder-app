package com.op1m.medrem.backend_api.repository;

import org.springframework.stereotype.Repository;
import com.op1m.medrem.backend_api.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long>{
    Optional<User> findByUsername(String username);
    Optional<User> findUserByEmail(String email);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
    Optional<User> findByTelegramChatId(Long telergamChatId);
}
