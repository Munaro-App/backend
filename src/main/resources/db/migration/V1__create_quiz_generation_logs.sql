CREATE TABLE IF NOT EXISTS quiz_generation_logs (
    id BIGSERIAL PRIMARY KEY,
    tourist_spot_id BIGINT REFERENCES tourist_spots(tourist_spot_id),
    status VARCHAR(20) NOT NULL,
    error_message TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_quiz_generation_logs_tourist_spot
    ON quiz_generation_logs (tourist_spot_id);
