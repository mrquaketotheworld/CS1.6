(ns db.models.map-server
  (:require [db.connection :as db]))

(defn insert-main-maps [client server-id]
  (.query
     client
     (str "INSERT INTO map_server (map, server_id, is_main)"
          "VALUES"
          "('Nuke', $1, true),"
          "('Mirage', $1, true),"
          "('Dust2', $1, true),"
          "('Tuscan', $1, true),"
          "('Train', $1, true),"
          "('Cache', $1, true),"
          "('Inferno', $1, true)"
          ) #js [server-id]))

(defn insert-extra-maps [client server-id]
  (.query
     client
     (str "INSERT INTO map_server (map, server_id, is_extra)"
          "VALUES"
          "('Aztec', $1, true),"
          "('Cbble', $1, true),"
          "('CPLFire', $1, true),"
          "('CPLMill', $1, true),"
          "('Dust', $1, true),"
          "('Overpass', $1, true),"
          "('Vertigo', $1, true),"
          "('Prodigy', $1, true)"
          ) #js [server-id]))

(defn insert-fun-maps [client server-id]
  (.query
     client
     (str "INSERT INTO map_server (map, server_id, is_fun)"
          "VALUES"
          "('Assault', $1, true),"
          "('HEKrystal', $1, true),"
          "('IceWorld', $1, true),"
          "('Mansion', $1, true),"
          "('PoolDay', $1, true),"
          "('PoolDay2', $1, true)") #js [server-id]))

(defn insert-default-maps [client server-id]
  (.. (insert-main-maps client server-id)
      (then #(insert-extra-maps client server-id))
      (then #(insert-fun-maps client server-id))))

(defn check-server-with-maps-exists [server-id]
  (.query db/pool "SELECT * FROM map_server WHERE server_id = $1" #js [server-id]))

(defn select-maps [server-id maptype]
  (.query db/pool
      (str "SELECT * FROM map_server WHERE server_id = $1 AND is_" maptype " = true")
      #js [server-id]))

(defn select-main-maps [server-id]
  (select-maps server-id "main"))

(defn select-extra-maps [server-id]
  (select-maps server-id "extra"))

(defn select-fun-maps [server-id]
  (select-maps server-id "fun"))
