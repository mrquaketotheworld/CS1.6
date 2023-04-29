CREATE TABLE player_server_points (
  id SERIAL PRIMARY KEY,
  player_id VARCHAR(255) NOT NULL REFERENCES player(player_id),
  server_id VARCHAR(255) NOT NULL REFERENCES server(server_id),
  points INT DEFAULT 128 NOT NULL,
  created_at timestamptz DEFAULT NOW() NOT NULL
);
