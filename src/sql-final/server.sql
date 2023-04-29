CREATE TABLE server (
  server_id VARCHAR(255) PRIMARY KEY,
  server VARCHAR(100) NOT NULL,
  created_at timestamptz DEFAULT NOW() NOT NULL
);
