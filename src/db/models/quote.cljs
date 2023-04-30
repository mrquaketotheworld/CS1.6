(ns db.models.quote
  (:require [db.connection :as db]))


(defn get-quotes-query []
  (.query db/pool "SELECT * FROM quote"))

(defn search-word-quote [word]
  (.query
    db/pool
    "SELECT * FROM quote WHERE quote ILIKE '%'||$1||'%' OR author ILIKE '%'||$1||'%'" #js [word]))
