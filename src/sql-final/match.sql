CREATE TABLE match (
  id SERIAL PRIMARY KEY,
  map VARCHAR(100) NOT NULL REFERENCES map(map),
  team1_score INT NOT NULL,
  team2_score INT NOT NULL,
  team1 INT UNIQUE REFERENCES team(id),
  team2 INT UNIQUE REFERENCES team(id),
  created_at timestamptz DEFAULT NOW()
);
