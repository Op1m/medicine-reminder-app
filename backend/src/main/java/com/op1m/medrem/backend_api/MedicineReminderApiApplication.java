package com.op1m.medrem.backend_api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableCaching
@EnableAsync
@SpringBootApplication
@EnableScheduling
public class MedicineReminderApiApplication {
    public static void main(String[] args) {
		SpringApplication.run(MedicineReminderApiApplication.class, args);
        System.out.println("Сервис готов к работе!");
	}
}
