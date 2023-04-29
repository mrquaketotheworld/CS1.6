CREATE TABLE server (
  id SERIAL PRIMARY KEY,
  server_id VARCHAR(255) NOT NULL UNIQUE,
  server VARCHAR(100) NOT NULL,
  created_at timestamptz DEFAULT NOW() NOT NULL
);
