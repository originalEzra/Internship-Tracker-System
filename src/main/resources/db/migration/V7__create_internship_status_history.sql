CREATE TABLE internship_status_history (
    id BIGINT NOT NULL AUTO_INCREMENT,
    internship_id BIGINT NOT NULL,
    from_status VARCHAR(255) NOT NULL,
    to_status VARCHAR(255) NOT NULL,
    operator_user_id BIGINT,
    note VARCHAR(1000),
    created_at DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_status_history_internship_id
        FOREIGN KEY (internship_id)
        REFERENCES internships (id)
        ON DELETE CASCADE,
    CONSTRAINT fk_status_history_operator_user_id
        FOREIGN KEY (operator_user_id)
        REFERENCES users (id)
        ON DELETE SET NULL
);

CREATE INDEX idx_status_history_internship_created_at
    ON internship_status_history (internship_id, created_at);
