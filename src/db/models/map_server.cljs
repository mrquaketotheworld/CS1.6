(ns db.models.map-server
  (:require [db.connection :as db]))

(defn insert-main-maps [server-id]
  (.query db/pool
     (str "INSERT INTO map_server (map, server_id, maptype)"
          "VALUES"
          "('Nuke', $1, 'main'),"
          "('Mirage', $1, 'main'),"
          "('Dust2', $1, 'main'),"
          "('Tuscan', $1, 'main'),"
          "('Train', $1, 'main'),"
          "('Cache', $1, 'main'),"
          "('Inferno', $1, 'main')"
          ) #js [server-id]))

(defn insert-extra-maps [server-id]
  (.query db/pool
     (str "INSERT INTO map_server (map, server_id, maptype)"
          "VALUES"
          "('Aztec', $1, 'extra'),"
          "('Cbble', $1, 'extra'),"
          "('CPLFire', $1, 'extra'),"
          "('CPLMill', $1, 'extra'),"
          "('Dust', $1, 'extra'),"
          "('Overpass', $1, 'extra'),"
          "('Vertigo', $1, 'extra'),"
          "('Prodigy', $1, 'extra')"
          ) #js [server-id]))

(defn insert-fun-maps [server-id]
  (.query db/pool
     (str "INSERT INTO map_server (map, server_id, maptype)"
          "VALUES"
          "('Assault', $1, 'fun'),"
          "('HEKrystal', $1, 'fun'),"
          "('IceWorld', $1, 'fun'),"
          "('Mansion', $1, 'fun'),"
          "('PoolDay', $1, 'fun'),"
          "('PoolDay2', $1, 'fun')") #js [server-id]))

(defn insert-default-maps [server-id]
  (.. (insert-main-maps server-id)
      (then #(insert-extra-maps server-id))
      (then #(insert-fun-maps server-id))))

(defn check-server-with-maps-exists [server-id]
  (.query db/pool "SELECT * FROM map_server WHERE server_id = $1" #js [server-id]))

(defn select-maps [server-id maptype]
  (.query db/pool
      (str "SELECT * FROM map_server WHERE server_id = $1 AND maptype = $2 ORDER BY map")
      #js [server-id maptype]))
