package com.op1m.medrem.backend_api.config;

import com.op1m.medrem.backend_api.service.UserService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.core.userdetails.UserDetailsService;
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
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/auth/**", "/api/public/**", "/static/**", "/favicon.ico").permitAll()
                        .anyRequest().permitAll()
                );

        return http.build();
    }

    @Bean
    public UserDetailsService userDetailsService(UserService userService) {
        return username -> {

            com.op1m.medrem.backend_api.entity.User user =
                    userService.findByUsername(username);

            if (user == null) {
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
