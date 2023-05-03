(ns core
  (:require ["discord.js" :as discord]
            [config :refer [TOKEN]]
            [commands.quote :as quote]
            [commands.make-teams :as make-teams]
            [commands.go :as go-command]
            [commands.deploy-commands :as deploy]
            [commands.gg :as gg]))

(def client (discord/Client.
                 #js {:intents #js [(.-Guilds discord/GatewayIntentBits)
                                   (.-GuildVoiceStates discord/GatewayIntentBits)]}))

(def state (atom {:collectors {} }))

(defn handle-collector-event-type-button! [interaction]
  (let [command-name (.. interaction -message -interaction -commandName)]
    (case command-name
      "go"(go-command/handle-collector-event-type-button! interaction)
      "gg"(gg/interact! interaction)
      (println "OTHER"))
    ))

(defn init-collector-type-button [interaction]
  (let [channel-id (.. interaction -channel -id)]
    (when-not (get-in @state [:collectors channel-id])
        (swap! state assoc-in [:collectors channel-id]
               (.. interaction
                   -channel
                   (createMessageComponentCollector #js
                                                    {:componentType
                                                     (.-Button discord/ComponentType)})))
        (let [collector (get-in @state [:collectors channel-id])]
                              (.on collector "collect" handle-collector-event-type-button!)))))

(defn handle-interaction [interaction]
  (init-collector-type-button interaction)
  (println @state)
  ; (.log js/console (.. interaction -message))
  (when (.isChatInputCommand interaction)
    (case (.-commandName interaction)
      "quote" (quote/interact! interaction)
      "make-teams"(make-teams/interact! interaction)
      "go"(go-command/interact! interaction)
      "gg"(gg/interact! interaction)
      (println "OTHER"))))

(defn on-channel-delete [channel]
  (swap! state update-in [:collectors]
         (fn [old-collectors]
           (let [channel-id (.-id channel)]
           (.stop (old-collectors channel-id))
           (dissoc old-collectors channel-id)))))

(.on client "ready" #(println "Ready!" (js/Date.)))
(.on client "interactionCreate" handle-interaction)
(.on client "channelDelete" on-channel-delete)
(.login client TOKEN)

(defn main [& args]
  (when (= (nth args 0) "register")
    (println "Register commands")
    (deploy/register-commands)))

 (.on js/process "unhandledRejection"
      (fn [e] (println (js/Date.) "unhandledRejection" e)))

