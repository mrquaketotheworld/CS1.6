(ns commands.top
  (:require ["discord.js" :as discord]
            [db.models.player-server-points :as player-server-points]
            [cljs.core.async :refer [go]]
            [cljs.core.async.interop :refer-macros [<p!]]))

(def builder
  (.. (discord/SlashCommandBuilder.)
      (setName "top")
      (setDescription "Show top 10")
      toJSON))

(defn interact [interaction]
  (go (try
        (let [server-id (.. interaction -guild -id)]
          (<p! (.reply interaction server-id))
          )
        (catch js/Error e (println "ERROR interact top" e)))))

