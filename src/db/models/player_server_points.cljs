(ns db.models.player-server-points
  (:require [db.connection :as db]))

(defn select-player-by-server
  ([player-id server-id] (select-player-by-server db/pool player-id server-id))
  ([client player-id server-id]
  (.query client
  "SELECT player_id, points FROM player_server_points WHERE player_id = $1 AND server_id = $2"
    #js [player-id server-id])))

(defn select-players-points [client players-ids server-id]
  (.query client
 "SELECT player_id, points FROM player_server_points WHERE player_id = ANY ($1) AND server_id = $2"
    #js [players-ids server-id]))

(defn select-player-rating [player-id server-id]
  (.query db/pool
   "SELECT * FROM (SELECT *, DENSE_RANK () OVER (ORDER BY points DESC)
                  FROM player_server_points WHERE server_id = $1) AS sub WHERE sub.player_id = $2"
    #js [player-id server-id]))

(defn insert-player [client player-id server-id]
  (.query client
  "INSERT INTO player_server_points (player_id, server_id) VALUES ($1, $2)"
    #js [player-id server-id]))

(defn update-player-points [client player-id server-id points]
  (.query client
  "UPDATE player_server_points SET points = points + $1 WHERE player_id = $2 AND server_id = $3"
    #js [points player-id server-id]))


