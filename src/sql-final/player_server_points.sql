CREATE TABLE player_server_points (
  player_id VARCHAR(255) NOT NULL REFERENCES player(player_id),
  server_id VARCHAR(255) NOT NULL REFERENCES server(server_id),
  points DOUBLE PRECISION DEFAULT 128 NOT NULL,
  created_at timestamptz DEFAULT NOW() NOT NULL,
  PRIMARY KEY (player_id, server_id)
);
