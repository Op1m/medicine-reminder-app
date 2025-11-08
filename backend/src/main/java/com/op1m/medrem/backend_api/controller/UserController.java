package com.op1m.medrem.backend_api.controller;

import com.op1m.medrem.backend_api.entity.User;
import com.op1m.medrem.backend_api.service.UserService;
import com.op1m.medrem.backend_api.dto.UserDTO;
import com.op1m.medrem.backend_api.dto.DTOMapper;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import org.springframework.beans.factory.annotation.Autowired;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {
    @Autowired
    private UserService userService;

    @PostMapping("/register")
    public ResponseEntity<UserDTO> registerUser(@Valid @RequestBody UserRegistrationRequest request) {
        if(userService.existByUsername(request.getUsername())) {
            return new ResponseEntity<>(HttpStatus.CONFLICT);
        }

        if(userService.existByEmail(request.getEmail())) {
            return new ResponseEntity<>(HttpStatus.CONFLICT);
        }

        User newUser = userService.createUser(
                request.getUsername(),
                request.getPassword(),
                request.getEmail()
        );

        UserDTO userDTO = DTOMapper.toUserDTO(newUser);
        return new ResponseEntity<>(userDTO, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserDTO> getUserById(@PathVariable Long id) {
        User user = userService.findById(id);
        UserDTO userDTO = DTOMapper.toUserDTO(user);
        if(user != null) {
            return new ResponseEntity<>(userDTO, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @PostMapping("/{userId}/link-telegram")


    @PutMapping("/{id}")
    public ResponseEntity<UserDTO> updateUser(
            @PathVariable Long id,
            @Valid @RequestBody UserUpdateRequest request) {

        try {
            User updatedUser = userService.updateUser(
                    id,
                    request.getUsername(),
                    request.getEmail(),
                    request.getFirstName(),
                    request.getLastName()
            );
            UserDTO userDTO = DTOMapper.toUserDTO(updatedUser);
            return new ResponseEntity<>(userDTO, HttpStatus.OK);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/me")
    public ResponseEntity<UserDTO> getCurrentUser(Authentication authentication) {
        try {
            String username = authentication.getName();
            User user = userService.getCurrentUser(username);
            UserDTO userDTO = DTOMapper.toUserDTO(user);
            return new ResponseEntity<>(userDTO, HttpStatus.OK);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @PatchMapping("/{id}/password")
    public ResponseEntity<Void> changePassword(
            @PathVariable Long id,
            @Valid @RequestBody PasswordChangeRequest request) {

        try {
            userService.changePassword(id, request.getOldPassword(), request.getNewPassword());
            return new ResponseEntity<>(HttpStatus.OK);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    @PatchMapping("/me/password")
    public ResponseEntity<Void> changeCurrentUserPassword(
            Authentication authentication,
            @Valid @RequestBody PasswordChangeRequest request) {

        try {
            String username = authentication.getName();
            User user = userService.findByUsername(username);
            userService.changePassword(user.getId(), request.getOldPassword(), request.getNewPassword());
            return new ResponseEntity<>(HttpStatus.OK);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    @PatchMapping("/{id}/deactivate")
    public ResponseEntity<Void> deactivateUser(@PathVariable Long id) {
        try {
            userService.deactivateUser(id);
            return new ResponseEntity<>(HttpStatus.OK);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @PatchMapping("/{id}/activate")
    public ResponseEntity<Void> activateUser(@PathVariable Long id) {
        try {
            userService.activateUser(id);
            return new ResponseEntity<>(HttpStatus.OK);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    public ResponseEntity<UserDTO> linkTelegram (@PathVariable Long userId, @RequestBody TelegramLinkRequest request) {
        User user = userService.linkTelegramAccount(userId, request.getTelegramChatId());
        UserDTO userDTO = DTOMapper.toUserDTO(user);
        if(user != null) {
            return new ResponseEntity<>(userDTO, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    public static class UserRegistrationRequest {
        @NotBlank(message = "Username обязателен")
        @Size(min = 3, max = 50, message = "Username должен быть от 3 до 50 символов")
        private String username;

        @NotBlank(message = "Username обязателен")
        @Size(min = 6, message = "Password должен быть не менее 6 символов")
        private String password;

        @Email(message = "Некорректный email адрес")
        @NotBlank(message = "Email обязателен")
        private String email;

        public String getUsername() {return username;}
        public void setUsername(String username) {this.username = username;}

        public String getPassword() {return password;}
        public void setPassword(String password) {this.password = password;}

        public String getEmail() {return email;}
        public void setEmail(String email) {this.email = email;}
    }

    public static class TelegramLinkRequest {
        private Long telegramChatId;

        public Long getTelegramChatId() {return telegramChatId;}
        public void setTelegramChatId(Long telegramChatId) {this.telegramChatId = telegramChatId;}
    }

    public static class UserUpdateRequest {
        @NotBlank(message = "Username обязателен")
        @Size(min = 3, max = 50, message = "Username должен быть от 3 до 50 символов")
        private String username;

        @Email(message = "Некорректный email")
        @NotBlank(message = "Email обязателен")
        private String email;

        private String firstName;
        private String lastName;

        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }

        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }

        public String getFirstName() { return firstName; }
        public void setFirstName(String firstName) { this.firstName = firstName; }

        public String getLastName() { return lastName; }
        public void setLastName(String lastName) { this.lastName = lastName; }
    }

    public static class PasswordChangeRequest {
        @NotBlank(message = "Текущий пароль обязателен")
        private String oldPassword;

        @NotBlank(message = "Новый пароль обязателен")
        @Size(min = 6, message = "Новый пароль должен быть минимум 6 символов")
        private String newPassword;

        public String getOldPassword() { return oldPassword; }
        public void setOldPassword(String oldPassword) { this.oldPassword = oldPassword; }

        public String getNewPassword() { return newPassword; }
        public void setNewPassword(String newPassword) { this.newPassword = newPassword; }
    }
}



