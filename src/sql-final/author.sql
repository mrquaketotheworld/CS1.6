CREATE TABLE author (
  author VARCHAR(255) PRIMARY KEY,
  created_at timestamptz DEFAULT NOW() NOT NULL
);
