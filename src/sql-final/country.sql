CREATE TABLE country (
  country VARCHAR(100) PRIMARY KEY,
  created_at timestamptz DEFAULT NOW() NOT NULL
);
