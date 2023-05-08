CREATE TABLE rank (
  rank VARCHAR(255) PRIMARY KEY,
  points INT NOT NULL UNIQUE,
  created_at timestamptz DEFAULT NOW() NOT NULL
);
