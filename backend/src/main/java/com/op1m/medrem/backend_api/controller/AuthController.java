package com.op1m.medrem.backend_api.controller;

import com.op1m.medrem.backend_api.dto.UserDTO;
import com.op1m.medrem.backend_api.entity.User;
import com.op1m.medrem.backend_api.service.UserService;
import com.op1m.medrem.backend_api.dto.DTOMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    @Autowired
    private UserService userService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @PostMapping("/login")
    public ResponseEntity<UserDTO> login(@RequestBody LoginRequest request) {
        System.out.println("🔐 AuthController: Попытка входа: " + request.getUsername());

        User user = userService.findByUsername(request.getUsername());

        if (user != null && passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            UserDTO userDTO = DTOMapper.toUserDTO(user);
            System.out.println("✅ AuthController: Успешный вход: " + user.getUsername());
            return new ResponseEntity<>(userDTO, HttpStatus.OK);
        }

        System.out.println("❌ AuthController: Неверные credentials: " + request.getUsername());
        return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
    }

    @PostMapping("/register")
    public ResponseEntity<UserDTO> register(@RequestBody UserRegistrationRequest request) {
        try {
            System.out.println("👤 AuthController: Регистрация: " + request.getUsername());

            User newUser = userService.createUser(request.getUsername(), request.getPassword(), request.getEmail());
            UserDTO userDTO = DTOMapper.toUserDTO(newUser);
            System.out.println("✅ AuthController: Пользователь зарегистрирован: " + newUser.getUsername());
            return new ResponseEntity<>(userDTO, HttpStatus.CREATED);
        } catch (RuntimeException e ) {
            System.out.println("❌ AuthController: Ошибка регистрации: " + e.getMessage());
            return new ResponseEntity<>(HttpStatus.CONFLICT);
        }
    }

    public static class LoginRequest {
        private String username;
        private String password;
        private String email;

        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
    }

    public static class UserRegistrationRequest {
        private String username;
        private String password;
        private String email;

        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
    }
}
