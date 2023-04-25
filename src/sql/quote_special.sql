CREATE TABLE "quote_special" (
  id SERIAL PRIMARY KEY,
  quote TEXT NOT NULL,
  author VARCHAR(255) NOT NULL,
  created_at timestamptz DEFAULT NOW(),
  player_id VARCHAR(255) NOT NULL REFERENCES player(discord_id) -- FK player discord_id
);
