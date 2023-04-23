(ns core
  (:require ["discord.js" :as discord]
            [config :refer [TOKEN]]
            [commands.quote :as quote]
            [commands.make-random-teams :as make-random-teams]))

(def client (new discord/Client #js {:intents #js [(.-Guilds discord/GatewayIntentBits)
                                                   (.-GuildVoiceStates discord/GatewayIntentBits)]}))

(defn handle-interaction [^js/Object interaction]
  (when (.isChatInputCommand interaction)
    (case (.-commandName interaction)
      "quote" (quote/interact! interaction)
      "make-random-teams"(make-random-teams/interact! interaction)
      (js/console.log "OTHER"))))

(.on client "ready" #(js/console.log  "Ready!" (new js/Date)))
(.on client "interactionCreate" handle-interaction)
(.login client TOKEN)

(defn main [& args])
