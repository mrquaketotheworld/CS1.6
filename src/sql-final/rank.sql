CREATE TABLE rank (
  id SERIAL PRIMARY KEY,
  rank VARCHAR(255) NOT NULL UNIQUE, -- Pro
  points INT NOT NULL UNIQUE, -- 32
  color VARCHAR(100) NOT NULL UNIQUE, -- #ffffff
  created_at timestamptz DEFAULT NOW() NOT NULL
);
