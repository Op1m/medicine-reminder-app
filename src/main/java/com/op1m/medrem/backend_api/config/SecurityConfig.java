package com.op1m.medrem.backend_api.config;

import com.op1m.medrem.backend_api.service.UserService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity security, UserService userService) throws Exception {
        security
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(authz -> authz
                        .requestMatchers("/api/auth/**", "/api/public/**").permitAll()
                        .requestMatchers("/api/users/register").permitAll()
                        .requestMatchers("/api/users/me/**").authenticated()
                        .anyRequest().authenticated()
                )
                .userDetailsService(userDetailsService(userService)) // 👈 Используем ВАШИХ пользователей
                .httpBasic(httpBasic -> {});
        return security.build();
    }

    @Bean
    public UserDetailsService userDetailsService(UserService userService) {
        return username -> {
            System.out.println("=== SPRING SECURITY AUTH ===");
            System.out.println("🔐 Попытка аутентификации: " + username);

            com.op1m.medrem.backend_api.entity.User user = userService.findByUsername(username);

            if (user == null) {
                System.out.println("❌ Пользователь не найден в БД: " + username);
                throw new UsernameNotFoundException("User not found: " + username);
            }

            return User.builder()
                    .username(user.getUsername())
                    .password(user.getPassword())
                    .roles("USER")
                    .build();
        };
    }
}