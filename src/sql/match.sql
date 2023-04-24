CREATE TABLE match (
  id SERIAL PRIMARY KEY,
  name VARCHAR(100) NOT NULL,
  team1_score INT NOT NULL,
  team2_score INT NOT NULL,
  team1_player1_id VARCHAR(255) NOT NULL,
  team1_player2_id VARCHAR(255) NOT NULL,
  team1_player3_id VARCHAR(255) NOT NULL,
  team1_player4_id VARCHAR(255) NOT NULL,
  team1_player5_id VARCHAR(255) NOT NULL,
  team2_player1_id VARCHAR(255) NOT NULL,
  team2_player2_id VARCHAR(255) NOT NULL,
  team2_player3_id VARCHAR(255) NOT NULL,
  team2_player4_id VARCHAR(255) NOT NULL,
  team2_player5_id VARCHAR(255) NOT NULL,
  created_at timestamptz DEFAULT NOW()
);
