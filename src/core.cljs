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
      (.log js/console "OTHER"))))

(.on client "ready" #(.log js/console "Ready!" (new js/Date)))
(.on client "interactionCreate" handle-interaction)
(.login client TOKEN)

(defn main [& args]
  (println "hello world"))
