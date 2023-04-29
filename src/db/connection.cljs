(ns db.connection
  (:require ["pg" :as pg]))

(defonce pool (new pg/Pool #js {:user "rus"
                   :host "localhost"
                   :database "cs16"
                   :password "hi!"
                   :port 5432
                   }))


; const pool = new Pool({
;   user: 'me',
;   host: 'localhost',
;   database: 'api',
;   password: 'password',
;   port: 5432,
; })
