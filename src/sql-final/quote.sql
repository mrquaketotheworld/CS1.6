CREATE TABLE quote (
  id SERIAL PRIMARY KEY,
  quote TEXT NOT NULL,
  author VARCHAR(255) NOT NULL REFERENCES quote_author_check(author),
  created_at timestamptz DEFAULT NOW() NOT NULL
);
