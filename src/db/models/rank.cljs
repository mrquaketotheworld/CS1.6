(ns db.models.rank
  (:require [db.connection :as db]))

(defn select-rank-by-points [points]
  (.query db/pool "SELECT rank, color FROM rank WHERE points <= $1 ORDER BY points DESC LIMIT 1"
          #js [points]))


