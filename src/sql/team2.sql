CREATE TABLE team2 (
  id SERIAL PRIMARY KEY,
  player1 VARCHAR(255) NOT NULL,
  player2 VARCHAR(255) NOT NULL,
  player3 VARCHAR(255) NOT NULL,
  player4 VARCHAR(255) NOT NULL,
  player5 VARCHAR(255) NOT NULL,
  created_at timestamptz DEFAULT NOW(),
  match_id INT NOT NULL UNIQUE REFERENCES match(id) -- FK match id
);
