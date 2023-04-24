CREATE TABLE map_poll (
  id SERIAL PRIMARY KEY,
  name VARCHAR(100) NOT NULL,
  is_main BOOLEAN DEFAULT FALSE,
  is_extra BOOLEAN DEFAULT FALSE,
  is_fun BOOLEAN DEFAULT FALSE,
  discord_server_id VARCHAR(255) NOT NULL,
  created_at timestamptz DEFAULT NOW()
);

-- add default IDs and maps
