CREATE TABLE users (
                       id BIGSERIAL PRIMARY KEY,
                       username VARCHAR(50) UNIQUE NOT NULL,
                       password VARCHAR(255) NOT NULL,
                       email VARCHAR(100) UNIQUE NOT NULL,
                       telegram_chat_id BIGINT,
                       is_active BOOLEAN DEFAULT true,
                       created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                       updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE medicines (
                           id BIGSERIAL PRIMARY KEY,
                           name VARCHAR(100) NOT NULL,
                           dosage VARCHAR(50),
                           description TEXT,
                           instructions TEXT,
                           is_active BOOLEAN DEFAULT true,
                           created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                           updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE reminders (
                           id BIGSERIAL PRIMARY KEY,
                           user_id BIGINT NOT NULL REFERENCES users(id),
                           medicine_id BIGINT NOT NULL REFERENCES medicines(id),
                           reminder_time TIME NOT NULL,
                           is_active BOOLEAN DEFAULT true,
                           days_of_week VARCHAR(50) DEFAULT 'everyday',
                           created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                           updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE medicine_history (
                                  id BIGSERIAL PRIMARY KEY,
                                  reminder_id BIGINT NOT NULL REFERENCES reminders(id),
                                  scheduled_time TIMESTAMP NOT NULL,
                                  taken_at TIMESTAMP,
                                  status VARCHAR(20) DEFAULT 'PENDING',
                                  notes TEXT,
                                  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);