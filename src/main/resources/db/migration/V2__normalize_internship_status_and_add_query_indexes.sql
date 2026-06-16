UPDATE internships
SET status = UPPER(status)
WHERE status IS NOT NULL;

CREATE INDEX idx_internships_user_status ON internships (user_id, status);

CREATE INDEX idx_internships_user_created_at ON internships (user_id, created_at);
