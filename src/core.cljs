(ns core
  (:require [config :refer [TOKEN]]
            ["discord.js" :as discord]
            [commands.quote :as quote]))

(def client (new discord/Client #js {:intents #js [(.-Guilds discord/GatewayIntentBits)
                                                   (.-GuildVoiceStates discord/GatewayIntentBits)]}))

(defn handle-interaction [^js/Object interaction]
  (when (.isChatInputCommand interaction)
    (case (.-commandName interaction)
      "quote" (quote/quote interaction)
      (js/console.log "OTHER"))))

(.on client "ready" #(js/console.log  "Ready!" (new js/Date)))
(.on client "interactionCreate" handle-interaction)
(.login client TOKEN)

(defn main [& args]
  (println "hello world"))
