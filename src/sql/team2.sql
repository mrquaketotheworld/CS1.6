CREATE TABLE team2 (
  id SERIAL PRIMARY KEY,
  player_id VARCHAR(255) NOT NULL,
  created_at timestamptz DEFAULT NOW(),
  match_id INT REFERENCES match(id) -- FK match id
);
