(ns db.models.player-server-points)

(defn select-player-by-server [client player-id server_id]
  (.query client
  "SELECT player_id FROM player_server_points WHERE player_id = $1 AND server_id = $2"
    #js [player-id server_id]))

(defn select-players-points [client players-ids server_id]
  (.query client
 "SELECT player_id, points FROM player_server_points WHERE player_id = ANY ($1) AND server_id = $2"
    #js [players-ids server_id]))

(defn insert-player [client player-id server_id]
  (.query client
  "INSERT INTO player_server_points (player_id, server_id) VALUES ($1, $2)"
    #js [player-id server_id]))

