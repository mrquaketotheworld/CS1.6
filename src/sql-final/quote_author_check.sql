CREATE TABLE quote_author_check (
  id SERIAL PRIMARY KEY,
  author VARCHAR(255) NOT NULL UNIQUE,
  created_at timestamptz DEFAULT NOW() NOT NULL
);
