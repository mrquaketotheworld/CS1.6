CREATE TABLE team (
  id SERIAL PRIMARY KEY,
  created_at timestamptz DEFAULT NOW() NOT NULL
);
