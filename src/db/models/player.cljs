(ns db.models.player)

(defn insert-player-if-not-exists [client player-id player]
  (.query client
  "INSERT INTO player (player_id, player) VALUES ($1, $2) ON CONFLICT DO NOTHING"
    #js [player-id player]))

