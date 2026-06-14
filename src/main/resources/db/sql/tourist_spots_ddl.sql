CREATE TABLE IF NOT EXISTS tourist_spots (
    id BIGSERIAL PRIMARY KEY,
    tourist_spot_name VARCHAR(255) NOT NULL,
    description TEXT,
    latitude NUMERIC(11, 8) NOT NULL,
    longitude NUMERIC(12, 8) NOT NULL,
    address VARCHAR(500) NOT NULL,
    public_amenity_info TEXT,
    parking_capacity INTEGER,
    visitor_capacity INTEGER,
    management_phone VARCHAR(50),
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_tourist_spots_name
    ON tourist_spots (tourist_spot_name);

CREATE INDEX IF NOT EXISTS idx_tourist_spots_address
    ON tourist_spots (address);
