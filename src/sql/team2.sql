CREATE TABLE team2 (
  player_id VARCHAR(255) NOT NULL,
  match_id INT REFERENCES match(id)
);
