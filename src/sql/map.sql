CREATE TABLE map (
  id SERIAL PRIMARY KEY,
  map_name VARCHAR(100) NOT NULL,
  is_main BOOLEAN DEFAULT FALSE,
  is_extra BOOLEAN DEFAULT FALSE,
  is_fun BOOLEAN DEFAULT FALSE,
  created_at timestamptz DEFAULT NOW(),
  discord_server_id VARCHAR(255)
    REFERENCES discord_server(discord_server_id)
    ON DELETE CASCADE
);

-- add default IDs and maps
-- if server doesn't exist, add Default SQL
