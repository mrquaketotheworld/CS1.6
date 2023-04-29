CREATE TABLE quote (
  id SERIAL PRIMARY KEY,
  quote TEXT NOT NULL,
  author VARCHAR(255) NOT NULL REFERENCES author(author),
  created_at timestamptz DEFAULT NOW() NOT NULL
);
