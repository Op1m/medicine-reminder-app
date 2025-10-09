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

        String encodedPassword = passwordEncoder.encode(password);

        User user = new User(username,encodedPassword, email);
        User savedUser = userRepository.save(user);

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

}
