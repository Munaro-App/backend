ALTER TABLE seasons
    ADD COLUMN IF NOT EXISTS started_at TIMESTAMPTZ,
    ADD COLUMN IF NOT EXISTS ended_at TIMESTAMPTZ,
    ADD COLUMN IF NOT EXISTS closed_at TIMESTAMPTZ;

UPDATE seasons
SET started_at = COALESCE(started_at, NOW()),
    ended_at = COALESCE(ended_at, NOW() + INTERVAL '1 month')
WHERE started_at IS NULL
   OR ended_at IS NULL;

ALTER TABLE seasons
    ALTER COLUMN started_at SET NOT NULL,
    ALTER COLUMN ended_at SET NOT NULL;

CREATE TABLE IF NOT EXISTS season_ranking_snapshots (
    season_ranking_snapshot_id BIGSERIAL PRIMARY KEY,
    season_id BIGINT NOT NULL REFERENCES seasons(season_id),
    user_id BIGINT NOT NULL REFERENCES users(user_id),
    nickname VARCHAR(255) NOT NULL,
    score INTEGER NOT NULL,
    ranking INTEGER NOT NULL,
    snapshotted_at TIMESTAMPTZ NOT NULL,
    CONSTRAINT uk_season_ranking_snapshot_season_user
        UNIQUE (season_id, user_id)
);

CREATE INDEX IF NOT EXISTS idx_season_ranking_snapshots_season_rank
    ON season_ranking_snapshots (season_id, ranking);

CREATE TABLE IF NOT EXISTS season_winners (
    season_winner_id BIGSERIAL PRIMARY KEY,
    season_id BIGINT NOT NULL UNIQUE REFERENCES seasons(season_id),
    user_id BIGINT NOT NULL REFERENCES users(user_id),
    nickname VARCHAR(255) NOT NULL,
    score INTEGER NOT NULL,
    decided_at TIMESTAMPTZ NOT NULL
);
