CREATE TABLE player_team (
  id SERIAL PRIMARY KEY,
  player_id VARCHAR(255) NOT NULL REFERENCES player(player_id),
  team_id INT NOT NULL REFERENCES team(id),
  server_id VARCHAR(255) NOT NULL REFERENCES server(server_id),
  created_at timestamptz DEFAULT NOW(),
);
