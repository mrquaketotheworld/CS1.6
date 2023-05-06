(ns db.models.match)

(defn insert-match [client map-name team1-score team2-score team1-id team2-id]
  (.query client (str "INSERT INTO match (map, team1_score, team2_score, team1, team2)"
                      "VALUES ($1, $2, $3, $4, $5)"
                ) #js [map-name team1-score team2-score team1-id team2-id]))


