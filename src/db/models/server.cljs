(ns db.models.server
  (:require [db.connection :as db]))

(defn insert-server-if-not-exists [server-id server]
  (.query db/pool
          "INSERT INTO server (server_id, server) VALUES ($1, $2) ON CONFLICT DO NOTHING"
          #js [server-id server]))
