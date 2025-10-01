package com.op1m.medrem.backend_api.service;

import com.op1m.medrem.backend_api.entity.User;
import java.util.List;

public interface UserService {
    User createUser(String username, String password, String email);
    User findByUsername (String username);
    User findById (Long id);
    List<User> getAllUsers();
    User updateUser(Long id, String username, String email);
    User linkTelegramAccount(Long userId, Long telegramChatId);
    boolean existByUsername(String username);
    boolean existByEmail(String email);
}
