(ns db.models.match
  (:require [db.connection :as db]))

(defn insert-match [client map-name team1-score team2-score team1-id team2-id]
  (.query client (str "INSERT INTO match (map, team1_score, team2_score, team1, team2)"
                      "VALUES ($1, $2, $3, $4, $5) RETURNING id")
          #js [map-name team1-score team2-score team1-id team2-id]))

(defn select-team1-wins [team-ids]
  (.query db/pool "SELECT map, team1_score, team2_score FROM match WHERE team1 = ANY ($1)
        AND team1_score > team2_score" #js [team-ids]))

(defn select-team1-losses [team-ids]
  (.query db/pool "SELECT map, team1_score, team2_score FROM match WHERE team1 = ANY ($1)
          AND team1_score < team2_score" #js [team-ids]))

(defn select-team1-draws [team-ids]
  (.query db/pool "SELECT map, team1_score, team2_score FROM match WHERE team1 = ANY ($1)
          AND team1_score = team2_score" #js [team-ids]))

(defn select-team2-wins [team-ids]
  (.query db/pool "SELECT map, team2_score, team1_score FROM match WHERE team2 = ANY ($1)
          AND team1_score < team2_score" #js [team-ids]))

(defn select-team2-losses [team-ids]
  (.query db/pool "SELECT map, team2_score, team1_score FROM match WHERE team2 = ANY ($1)
          AND team1_score > team2_score" #js [team-ids]))

(defn select-team2-draws [team-ids]
  (.query db/pool "SELECT map, team2_score, team1_score FROM match WHERE team2 = ANY ($1)
          AND team1_score = team2_score" #js [team-ids]))

