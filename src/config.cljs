(ns config
  (:require
   ["dotenv" :as dotenv]))

(def config ((js->clj (.config dotenv)) "parsed"))
(def TOKEN (config "TOKEN"))
(def CLIENT_ID (config "CLIENT_ID"))
