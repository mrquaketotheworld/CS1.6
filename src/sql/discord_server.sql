CREATE TABLE discord_server (
  id SERIAL PRIMARY KEY,
  discord_server_id VARCHAR(255) NOT NULL UNIQUE,
  discord_name VARCHAR(100) NOT NULL,
  created_at timestamptz DEFAULT NOW()
);
