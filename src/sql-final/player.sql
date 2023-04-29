CREATE TABLE player (
  id SERIAL PRIMARY KEY,
  player_id VARCHAR(255) NOT NULL UNIQUE,
  player VARCHAR(255) NOT NULL,
  nanax_points INT DEFAULT 0 NOT NULL,
  tag VARCHAR(255) DEFAULT '?' NOT NULL,
  country VARCHAR(255) DEFAULT '?' NOT NULL,
  created_at timestamptz DEFAULT NOW() NOT NULL,
  updated_at timestamptz DEFAULT NOW() NOT NULL
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
