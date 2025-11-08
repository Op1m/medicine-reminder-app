# Medicine Reminder App

Мобильное приложение для напоминаний о приёме лекарств.

## Технологии
- **Backend:** Spring Boot, Java 21
- **Database:** H2 (development), PostgreSQL (production) 
- **Mobile:** Android (Kotlin)
- **Web:** Thymeleaf/Bootstrap
- **Telegram:** Telegram Mini Apps

## Архитектура
[Telegram Bot] ←→ [Spring Boot] ←→ [Database]
↑ ↑ ↑
[Android App] ←→ [REST API] ←→ [Web Site]
