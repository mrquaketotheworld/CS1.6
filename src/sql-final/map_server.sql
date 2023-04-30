CREATE TABLE map_server (
  id SERIAL PRIMARY KEY,
  map VARCHAR(100) NOT NULL REFERENCES map(map), -- Nuke
  server_id VARCHAR(255) NOT NULL REFERENCES server(server_id),
  maptype VARCHAR(100) NOT NULL REFERENCES maptype(maptype),
  created_at timestamptz DEFAULT NOW() NOT NULL
);
