CREATE TABLE map (
  id SERIAL PRIMARY KEY,
  name VARCHAR(100) NOT NULL,
  is_main BOOLEAN DEFAULT FALSE,
  is_extra BOOLEAN DEFAULT FALSE,
  is_fun BOOLEAN DEFAULT FALSE,
  discord_server_id VARCHAR(255) DEFAULT 'default',
  created_at timestamptz DEFAULT NOW()
);

-- add default IDs and maps
