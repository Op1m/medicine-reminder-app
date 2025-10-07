package com.op1m.medrem.backend_api.controller;

import com.op1m.medrem.backend_api.entity.User;
import com.op1m.medrem.backend_api.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {
    @Autowired
    private UserService userService;

    @PostMapping("/register")
    public ResponseEntity<User> registerUser(@RequestBody UserRegistrationRequest request) {
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

        return new ResponseEntity<>(newUser, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<User> getUserById(@PathVariable Long id) {
        User user = userService.findById(id);

        if(user != null) {
            return new ResponseEntity<>(user, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @PostMapping("/{userId}/link-telegram")
    public ResponseEntity<User> linkTelegram (@PathVariable Long userId, @RequestBody TelegramLinkRequest request) {
        User user = userService.linkTelegramAccount(userId, request.getTelegramChatId());

        if(user != null) {
            return new ResponseEntity<>(user, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    public static class UserRegistrationRequest {
        private String username;
        private String password;
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
}



