ALTER TABLE internships
    ADD COLUMN updated_at DATETIME(6);

UPDATE internships
SET updated_at = COALESCE(created_at, CURRENT_TIMESTAMP(6))
WHERE updated_at IS NULL;
