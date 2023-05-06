(ns db.models.team)

(defn insert-generate-team-id [client]
  (.query client "INSERT INTO team DEFAULT VALUES RETURNING id")
