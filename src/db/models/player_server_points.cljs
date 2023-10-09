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
          "SELECT dense_rank FROM (SELECT player_id, DENSE_RANK () OVER (ORDER BY points DESC)
                  FROM player_server_points WHERE server_id = $1) AS sub WHERE sub.player_id = $2"
          #js [server-id player-id]))

(defn insert-player [client player-id server-id]
  (.query client
          "INSERT INTO player_server_points (player_id, server_id) VALUES ($1, $2)"
          #js [player-id server-id]))

(defn update-player-points [client player-id server-id points]
  (.query client "call update_player_points($1, $2, $3)" #js [points player-id server-id]))

(defn select-top-10 [server-id]
  (.query db/pool
          "SELECT player.player_id, player.player, sub.dense_rank, player_server_points.points
          FROM player JOIN
          (SELECT player_id, DENSE_RANK () OVER (ORDER BY points DESC)
                  FROM player_server_points WHERE server_id = $1)
          AS sub ON player.player_id = sub.player_id
          JOIN player_server_points ON player.player_id = player_server_points.player_id
          ORDER BY dense_rank LIMIT 10" #js [server-id]))
