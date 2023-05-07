(ns db.models.player-server-points
  (:require [db.connection :as db]))

(defn select-player-by-server
  ([player-id server_id] (select-player-by-server db/pool player-id server_id))
  ([client player-id server_id]
  (.query client
  "SELECT player_id, points FROM player_server_points WHERE player_id = $1 AND server_id = $2"
    #js [player-id server_id])))

(defn select-players-points [client players-ids server_id]
  (.query client
 "SELECT player_id, points FROM player_server_points WHERE player_id = ANY ($1) AND server_id = $2"
    #js [players-ids server_id]))

(defn insert-player [client player-id server_id]
  (.query client
  "INSERT INTO player_server_points (player_id, server_id) VALUES ($1, $2)"
    #js [player-id server_id]))

(defn update-player-points [client player-id server_id points]
  (.query client
  "UPDATE player_server_points SET points = points + $1 WHERE player_id = $2 AND server_id = $3"
    #js [points player-id server_id]))


