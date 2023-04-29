(ns core
  (:require ["discord.js" :as discord]
            [config :refer [TOKEN]]
            [commands.quote :as quote]
            [db.connection :as db]
            [cljs.core.async :refer [go]]
            [cljs.core.async.interop :refer-macros [<p!]]
            [commands.make-random-teams :as make-random-teams]))

(def client (new discord/Client
                 #js {:intents #js [(.-Guilds discord/GatewayIntentBits)
                                   (.-GuildVoiceStates discord/GatewayIntentBits)]}))

; (go (try
;       (let [result (<p! (.query db/pool "SELECT * FROM rank WHERE rank = $1" #js ["Bot"]))]
;         (println (aget (.-rows result) 0))
;         )
;       (catch js/Error e (println e))))


(defn handle-interaction [^js/Object interaction]
  (when (.isChatInputCommand interaction)
    (case (.-commandName interaction)
      "quote" (quote/interact! interaction)
      "make-random-teams"(make-random-teams/interact! interaction)
      (println "OTHER"))))

(.on client "ready" #(println "Ready!" (new js/Date)))
(.on client "interactionCreate" handle-interaction)
(.login client TOKEN)

(defn main [& args])
