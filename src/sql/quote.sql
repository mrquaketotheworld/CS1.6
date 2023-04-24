CREATE TABLE "quote" (
  id SERIAL PRIMARY KEY,
  text TEXT NOT NULL,
  author VARCHAR(255) NOT NULL,
  player_id VARCHAR(255) DEFAULT 'default',
  created_at timestamptz DEFAULT NOW()
);
