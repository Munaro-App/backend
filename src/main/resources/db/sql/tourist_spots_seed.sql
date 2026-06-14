TRUNCATE TABLE tourist_spots RESTART IDENTITY;

\copy tourist_spots (
    tourist_spot_name,
    description,
    latitude,
    longitude,
    address,
    public_amenity_info,
    parking_capacity,
    visitor_capacity,
    management_phone,
    created_at,
    updated_at
) FROM 'src/main/resources/db/seed/tourist_spots.csv' WITH (FORMAT csv, HEADER true);

SELECT
    id,
    tourist_spot_name,
    latitude,
    longitude,
    address,
    parking_capacity,
    visitor_capacity
FROM tourist_spots
ORDER BY id
LIMIT 5;
