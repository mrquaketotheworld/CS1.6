CREATE TABLE player (
  id SERIAL PRIMARY KEY,
  player_id VARCHAR(255) NOT NULL UNIQUE,
  player VARCHAR(255) NOT NULL,
  nanax_points INT DEFAULT 0,
  tag VARCHAR(255) DEFAULT '?',
  country VARCHAR(255) DEFAULT '?',
  created_at timestamptz DEFAULT NOW(),
  updated_at timestamptz DEFAULT NOW()
);

CREATE OR REPLACE FUNCTION trigger_set_timestamp()
RETURNS TRIGGER AS $$
BEGIN
  NEW.updated_at = NOW();
  RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER set_timestamp
BEFORE UPDATE ON player
FOR EACH ROW
  EXECUTE PROCEDURE trigger_set_timestamp();
