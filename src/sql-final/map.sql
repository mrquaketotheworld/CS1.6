CREATE TABLE map (
  map VARCHAR(100) PRIMARY KEY, -- Nuke
  created_at timestamptz DEFAULT NOW() NOT NULL
);
