(ns db.models.player-team-server)

(defn insert-team [client player1-id player2-id player3-id player4-id player5-id team-id server-id]
  (.query client (str "INSERT INTO player_team_server (player_id, team_id, server_id)"
                      "VALUES"
                      "($1, $6, $7),"
                      "($2, $6, $7),"
                      "($3, $6, $7),"
                      "($4, $6, $7),"
                      "($5, $6, $7)"
                ) #js [player1-id player2-id player3-id player4-id player5-id team-id server-id]))

