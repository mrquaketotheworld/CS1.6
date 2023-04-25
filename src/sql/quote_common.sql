CREATE TABLE "quote_common" (
  id SERIAL PRIMARY KEY,
  quote TEXT NOT NULL,
  author VARCHAR(255) NOT NULL,
  created_at timestamptz DEFAULT NOW()
);
