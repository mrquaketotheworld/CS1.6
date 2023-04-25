CREATE TABLE match (
  id SERIAL PRIMARY KEY,
  map_name VARCHAR(100) NOT NULL,
  team1_score INT NOT NULL,
  team2_score INT NOT NULL,
  created_at timestamptz DEFAULT NOW()
);
