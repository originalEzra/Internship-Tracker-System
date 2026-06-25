CREATE TABLE notifications (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    type VARCHAR(50) NOT NULL,
    title VARCHAR(200) NOT NULL,
    content VARCHAR(1000) NOT NULL,
    is_read BOOLEAN NOT NULL DEFAULT FALSE,
    source_type VARCHAR(50) NOT NULL,
    source_id BIGINT NOT NULL,
    created_at DATETIME(6) NOT NULL,
    read_at DATETIME(6),
    CONSTRAINT fk_notifications_user
        FOREIGN KEY (user_id) REFERENCES users(id)
        ON DELETE CASCADE,
    CONSTRAINT uk_notifications_source
        UNIQUE (source_type, source_id)
);

CREATE INDEX idx_notifications_user_read_created_at
    ON notifications (user_id, is_read, created_at);

