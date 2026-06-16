CREATE TABLE users (
    id BIGINT NOT NULL AUTO_INCREMENT,
    username VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL,
    password VARCHAR(255) NOT NULL,
    created_at DATETIME(6),
    PRIMARY KEY (id),
    CONSTRAINT uk_users_username UNIQUE (username),
    CONSTRAINT uk_users_email UNIQUE (email)
);

CREATE TABLE internships (
    id BIGINT NOT NULL AUTO_INCREMENT,
    company VARCHAR(255),
    position VARCHAR(255),
    location VARCHAR(255),
    status VARCHAR(255),
    application_url VARCHAR(255),
    created_at DATETIME(6),
    user_id BIGINT NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_internships_user_id
        FOREIGN KEY (user_id)
        REFERENCES users (id)
);

CREATE INDEX idx_internships_user_id ON internships (user_id);
