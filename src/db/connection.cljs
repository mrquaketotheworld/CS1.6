(ns db.connection
  (:require ["pg" :as pg]
            [config :refer [PG_USER PG_HOST PG_DATABASE PG_PASSWORD PG_PORT]]))

(defonce pool (pg/Pool. #js {:user PG_USER
                   :host PG_HOST
                   :database PG_DATABASE
                   :password PG_PASSWORD
                   :port PG_PORT
                   }))

(defn begin-transaction [client]
  (.query client "BEGIN"))

(defn rollback-transaction [client]
  (.query client "ROLLBACK"))

(defn commit-transaction [client]
  (.query client "COMMIT"))
