CREATE TABLE maptype (
  maptype VARCHAR(100) PRIMARY KEY,
  created_at timestamptz DEFAULT NOW() NOT NULL
);
