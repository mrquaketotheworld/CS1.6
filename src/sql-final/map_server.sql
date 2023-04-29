CREATE TABLE map_server (
  id SERIAL PRIMARY KEY,
  map VARCHAR(100) NOT NULL REFERENCES map(map), -- Nuke
  server_id VARCHAR(255) NOT NULL REFERENCES server(server_id),
  is_main BOOLEAN NOT NULL DEFAULT FALSE,
  is_fun BOOLEAN NOT NULL DEFAULT FALSE,
  is_extra BOOLEAN NOT NULL DEFAULT FALSE,
  created_at timestamptz DEFAULT NOW() NOT NULL
);
