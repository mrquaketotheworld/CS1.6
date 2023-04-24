CREATE TABLE "map" (
  id SERIAL PRIMARY KEY,
  name VARCHAR(100) NOT NULL,
  quantity INT DEFAULT 0,
  player_id VARCHAR(255) UNIQUE NOT NULL,
  created_at timestamptz DEFAULT NOW()
);
