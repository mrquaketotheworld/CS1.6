CREATE TABLE map_history (
  id SERIAL PRIMARY KEY,
  name VARCHAR(100) NOT NULL,
  quantity INT DEFAULT 0,
  wins INT DEFAULT 0,
  losses INT DEFAULT 0,
  draws INT DEFAULT 0,
  player_id VARCHAR(255) NOT NULL,
  created_at timestamptz DEFAULT NOW()
);
