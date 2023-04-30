(ns core
  (:require ["discord.js" :as discord]
            [config :refer [TOKEN]]
            [commands.quote :as quote]
            [commands.make-teams :as make-teams]
            [commands.go :as go-command]
            [commands.deploy-commands :as deploy]))

(def client (discord/Client.
                 #js {:intents #js [(.-Guilds discord/GatewayIntentBits)
                                   (.-GuildVoiceStates discord/GatewayIntentBits)]}))

(defn handle-interaction [^js/Object interaction]
  (when (.isChatInputCommand interaction)
    (case (.-commandName interaction)
      "quote" (quote/interact! interaction)
      "make-teams"(make-teams/interact! interaction)
      "go"(go-command/interact! interaction)
      (println "OTHER"))))

(.on client "ready" #(println "Ready!" (js/Date.)))
(.on client "interactionCreate" handle-interaction)
(.login client TOKEN)

(defn main [& args]
  (when (= (nth args 0) "register")
    (println "Register commands")
    (deploy/register-commands)))
