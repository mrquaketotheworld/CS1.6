CREATE TABLE rank (
  rank VARCHAR(255) PRIMARY KEY, -- Pro
  points INT NOT NULL UNIQUE, -- 32
  color VARCHAR(100) NOT NULL UNIQUE, -- #ffffff
  created_at timestamptz DEFAULT NOW() NOT NULL
);
