ALTER TABLE seasons
    ADD COLUMN IF NOT EXISTS season_id BIGSERIAL,
    ADD COLUMN IF NOT EXISTS season_name VARCHAR(255),
    ADD COLUMN IF NOT EXISTS started_at TIMESTAMPTZ,
    ADD COLUMN IF NOT EXISTS ended_at TIMESTAMPTZ,
    ADD COLUMN IF NOT EXISTS created_at TIMESTAMPTZ,
    ADD COLUMN IF NOT EXISTS active BOOLEAN;

UPDATE seasons
SET season_name = COALESCE(season_name, TO_CHAR(started_at, 'YYYY-MM') || ' 시즌'),
    started_at = COALESCE(started_at, NOW()),
    ended_at = COALESCE(ended_at, NOW() + INTERVAL '1 month'),
    created_at = COALESCE(created_at, NOW()),
    active = COALESCE(active, false)
WHERE season_name IS NULL
   OR started_at IS NULL
   OR ended_at IS NULL
   OR created_at IS NULL
   OR active IS NULL;

ALTER TABLE seasons
    ALTER COLUMN season_name SET NOT NULL,
    ALTER COLUMN started_at SET NOT NULL,
    ALTER COLUMN ended_at SET NOT NULL,
    ALTER COLUMN created_at SET NOT NULL,
    ALTER COLUMN active SET NOT NULL;

ALTER TABLE seasons
    ALTER COLUMN active SET DEFAULT true;

CREATE UNIQUE INDEX IF NOT EXISTS uk_seasons_season_id
    ON seasons (season_id);

ALTER TABLE scores
    ADD COLUMN IF NOT EXISTS score_id BIGSERIAL,
    ADD COLUMN IF NOT EXISTS season_id BIGINT,
    ADD COLUMN IF NOT EXISTS source VARCHAR(255),
    ADD COLUMN IF NOT EXISTS submission_id BIGINT,
    ADD COLUMN IF NOT EXISTS points INTEGER,
    ADD COLUMN IF NOT EXISTS created_at TIMESTAMPTZ;

UPDATE scores
SET points = COALESCE(points, 0),
    created_at = COALESCE(created_at, NOW())
WHERE points IS NULL
   OR created_at IS NULL;

CREATE TABLE IF NOT EXISTS rankings (
    ranking_id BIGSERIAL PRIMARY KEY,
    season_id BIGINT NOT NULL REFERENCES seasons(season_id),
    user_id BIGINT NOT NULL REFERENCES users(user_id),
    nickname VARCHAR(255) NOT NULL,
    score INTEGER NOT NULL,
    rank INTEGER NOT NULL,
    snapshotted_at TIMESTAMPTZ NOT NULL,
    CONSTRAINT uk_rankings_season_user
        UNIQUE (season_id, user_id)
);

ALTER TABLE rankings
    ADD COLUMN IF NOT EXISTS season_id BIGINT,
    ADD COLUMN IF NOT EXISTS user_id BIGINT,
    ADD COLUMN IF NOT EXISTS nickname VARCHAR(255),
    ADD COLUMN IF NOT EXISTS score INTEGER,
    ADD COLUMN IF NOT EXISTS rank INTEGER,
    ADD COLUMN IF NOT EXISTS snapshotted_at TIMESTAMPTZ;

CREATE INDEX IF NOT EXISTS idx_rankings_season_rank
    ON rankings (season_id, rank);

CREATE UNIQUE INDEX IF NOT EXISTS uk_rankings_season_user
    ON rankings (season_id, user_id);

CREATE TABLE IF NOT EXISTS season_winners (
    season_winner_id BIGSERIAL PRIMARY KEY,
    season_id BIGINT NOT NULL UNIQUE REFERENCES seasons(season_id),
    user_id BIGINT NOT NULL REFERENCES users(user_id),
    nickname VARCHAR(255) NOT NULL,
    score INTEGER NOT NULL,
    decided_at TIMESTAMPTZ NOT NULL
);
