CREATE TABLE map (
  id SERIAL PRIMARY KEY,
  map VARCHAR(100) NOT NULL UNIQUE, -- Nuke
  created_at timestamptz DEFAULT NOW() NOT NULL
);
