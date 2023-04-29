CREATE TABLE rank (
  rank VARCHAR(255) PRIMARY KEY, -- Pro
  points INT PRIMARY KEY, -- 32
  color VARCHAR(100) PRIMARY KEY, -- #ffffff
  created_at timestamptz DEFAULT NOW() NOT NULL
);
