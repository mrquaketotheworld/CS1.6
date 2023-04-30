(ns db.models.server)

(defn insert-server-if-not-exists [client server-id server]
  (.query client
          "INSERT INTO server (server_id, server) VALUES ($1, $2) ON CONFLICT DO NOTHING"
          #js [server-id server]))
