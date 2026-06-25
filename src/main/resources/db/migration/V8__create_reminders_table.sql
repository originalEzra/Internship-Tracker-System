CREATE TABLE reminders (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    internship_id BIGINT NOT NULL,
    message VARCHAR(500) NOT NULL,
    remind_at DATETIME(6) NOT NULL,
    status VARCHAR(20) NOT NULL,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    CONSTRAINT fk_reminders_user
        FOREIGN KEY (user_id) REFERENCES users(id)
        ON DELETE CASCADE,
    CONSTRAINT fk_reminders_internship
        FOREIGN KEY (internship_id) REFERENCES internships(id)
        ON DELETE CASCADE
);

CREATE INDEX idx_reminders_user_status_remind_at
    ON reminders (user_id, status, remind_at);

CREATE INDEX idx_reminders_internship_id
    ON reminders (internship_id);
