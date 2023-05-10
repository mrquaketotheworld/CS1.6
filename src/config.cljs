(ns config
  (:require
   ["dotenv" :as dotenv]))

(def config ((js->clj (.config dotenv)) "parsed"))
(def TOKEN (config "TOKEN"))
(def CLIENT_ID (config "CLIENT_ID"))
(def PG_USER (config "PG_USER"))
(def PG_HOST (config "PG_HOST"))
(def PG_DATABASE (config "PG_DATABASE"))
(def PG_PASSWORD (config "PG_PASSWORD"))
(def PG_PORT (config "PG_PORT"))
(def GUILD (config "GUILD"))
(def GUILD_ADMIN (config "GUILD_ADMIN"))
(def GUILD_SCORE (config "GUILD_SCORE"))
(def GUILD_CHANNEL_SCORE (config "GUILD_CHANNEL_SCORE"))
