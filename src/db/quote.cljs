(ns db.quote
  (:require [db.connection :as db]))


(defn get-quotes-query []
  (.query db/pool "SELECT * FROM quote"))



