(ns db.models.player
  (:require [db.connection :as db]))

(defn insert-player-if-not-exists [client player-id player]
  (.query client "INSERT INTO player (player_id, player) VALUES ($1, $2) ON CONFLICT DO NOTHING"
    #js [player-id player]))

(defn select-player [player-id]
  (.query db/pool "SELECT tag FROM player WHERE player_id = $1" #js [player-id]))

(defn update-player [player-id column value]
  (.query db/pool (str "UPDATE player SET " column " = $2 WHERE player_id = $1")
          #js [player-id value]))

