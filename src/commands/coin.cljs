(ns commands.coin
  (:require ["discord.js" :as discord]
            [cljs.core.async :refer [go]]
            [cljs.core.async.interop :refer-macros [<p!]]))

(def builder
  (.. (discord/SlashCommandBuilder.)
      (setName "coin")
      (setDescription "Toss a coin")
      toJSON))

(defn interact! [interaction]
  (go (try
        (<p! (.reply interaction #js {:content (get ["Heads!", "Tails!"] (rand-int 2))}))
        (catch js/Error e (println "ERROR coin" e)))))
