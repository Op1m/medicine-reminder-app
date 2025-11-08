package com.op1m.medrem.backend_api.service.impl;

import com.op1m.medrem.backend_api.entity.User;
import com.op1m.medrem.backend_api.repository.UserRepository;
import com.op1m.medrem.backend_api.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

@Service
public class UserServiceImpl implements UserService  {

    private final UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    public UserServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public User createUser(String username, String password, String email) {
        if(existByUsername(username)) {
            throw new RuntimeException("Пользователь с username '" + username + "' уже существует");
        }

        if(existByEmail(email)) {
            throw new RuntimeException("Пользователь с email '" + email + "' уже существует");
        }

        System.out.println("🔐 Создание пользователя: " + username);
        System.out.println("🔑 Исходный пароль: " + password);

        String encodedPassword = passwordEncoder.encode(password);
        System.out.println("🔑 Закодированный пароль: " + encodedPassword);

        User user = new User(username, encodedPassword, email);
        User savedUser = userRepository.save(user);

        System.out.println("✅ Пользователь создан: " + savedUser.getId());

        return savedUser;
    }

    @Override
    public User findByUsername (String username) {
        Optional<User> user = userRepository.findByUsername(username);

        if(user.isPresent()) {
            return user.get();
        } else {
            return null;
        }
    }

    @Override
    public User findById(Long id) {
        Optional<User> user = userRepository.findById(id);

        if(user.isPresent()) {
            return user.get();
        } else {
            return null;
        }
    }

    @Override
    public List<User> getAllUsers() {
        List<User> users = userRepository.findAll();
        return users;
    }

    @Override
    public User updateUser(Long id, String username, String email) {
        User user = findById(id);
        if(user == null) {
            throw new RuntimeException("Пользователь с ID " + id + " не найден");
        }

        if(!user.getUsername().equals(username) && existByUsername(username)) {
            throw new RuntimeException("Username '" + username + "' уже занят");
        }

        if(!user.getEmail().equals(email) && existByEmail(email)) {
            throw new RuntimeException("Email '" + email + "' уже используется");
        }

        user.setUsername(username);
        user.setEmail(email);

        User updatedUser = userRepository.save(user);
        return updatedUser;
    }

    @Override
    public User linkTelegramAccount(Long userId, Long telegramChatId) {
        User user = findById(userId);
        if(user == null) {
            throw new RuntimeException("Пользователь с ID " + userId + " не найден");
        }

        Optional<User> existingUser = userRepository.findByTelegramChatId(telegramChatId);
        if(existingUser.isPresent() && !existingUser.get().getId().equals(userId)) {
            throw new RuntimeException("Этот Telegram аккаунт уже привязан к другому пользователю");
        }

        user.setTelegramChatId(telegramChatId);
        User updatedUser = userRepository.save(user);

        return updatedUser;
    }

    @Override
    public boolean existByUsername(String username) {
        return userRepository.existsByUsername(username);
    }

    @Override
    public boolean existByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    @Override
    public User updateUser(Long id, String username, String email, String firstName, String lastName) {
        System.out.println("✏️ UserService: Обновление пользователя: " + id);

        User user = findById(id);
        if (user == null) {
            throw new RuntimeException("❌ UserService: Пользователь с ID " + id + " не найден");
        }

        if (!user.getUsername().equals(username) && existByUsername(username)) {
            throw new RuntimeException("❌ UserService: Username '" + username + "' уже занят");
        }

        if (!user.getEmail().equals(email) && existByEmail(email)) {
            throw new RuntimeException("❌ UserService: Email '" + email + "' уже используется");
        }

        user.setUsername(username);
        user.setEmail(email);
        user.setFirstName(firstName);
        user.setLastName(lastName);

        User updatedUser = userRepository.save(user);
        System.out.println("✅ UserService: Пользователь обновлен: " + updatedUser.getId());
        return updatedUser;
    }

    @Override
    public void changePassword(Long userId, String oldPassword, String newPassword) {
        System.out.println("🔐 UserService: Смена пароля для пользователя: " + userId);

        User user = findById(userId);
        if (user == null) {
            throw new RuntimeException("❌ UserService: Пользователь не найден");
        }

        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new RuntimeException("❌ UserService: Текущий пароль неверен");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        System.out.println("✅ UserService: Пароль изменен для пользователя: " + userId);
    }

    @Override
    public User getCurrentUser(String username) {
        System.out.println("👤 UserService: Получение текущего пользователя: " + username);
        return findByUsername(username);
    }

    @Override
    public void deactivateUser(Long userId) {
        System.out.println("🚫 UserService: Деактивация пользователя: " + userId);

        User user = findById(userId);
        if (user == null) {
            throw new RuntimeException("❌ UserService: Пользователь не найден");
        }

        user.setActive(false);
        userRepository.save(user);
        System.out.println("✅ UserService: Пользователь деактивирован: " + userId);
    }

    @Override
    public void activateUser(Long userId) {
        System.out.println("✅ UserService: Активация пользователя: " + userId);

        User user = findById(userId);
        if (user == null) {
            throw new RuntimeException("❌ UserService: Пользователь не найден");
        }

        user.setActive(true);
        userRepository.save(user);
        System.out.println("✅ UserService: Пользователь активирован: " + userId);
    }

}
