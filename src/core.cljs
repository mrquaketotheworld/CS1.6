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

(def state (atom {:button-collectors {} :select-menu-collectors {} :user-select-collectors {}}))

(defn handle-collector-event-type-button! [interaction]
  (println 'BUTTON-COLLECTOR)
  (let [command-name (.. interaction -message -interaction -commandName)]
    (case command-name
      "go"(go-command/handle-collector-event-type-button! interaction)
      ; "gg"(gg/interact! interaction)
      (println "OTHER"))
    ))

(defn init-collector-type-button [interaction]
  (let [channel-id (.. interaction -channel -id)]
    (when-not (get-in @state [:button-collectors channel-id])
        (swap! state assoc-in [:button-collectors channel-id]
               (.. interaction
                   -channel
                   (createMessageComponentCollector #js
                                                    {:componentType
                                                     (.-Button discord/ComponentType)})))
        (let [collector (get-in @state [:button-collectors channel-id])]
                              (.on collector "collect" handle-collector-event-type-button!)))))

(defn handle-collector-event-select-menu! [interaction]
  (println 'SELECT-MENU-COLLECTOR)
  (let [command-name (.. interaction -message -interaction -commandName)]
    (case command-name
      ; "gg"(gg/interact! interaction)
      (println "OTHER"))
    ))

(defn init-collector-type-select-menu [interaction]
  (let [channel-id (.. interaction -channel -id)]
    (when-not (get-in @state [:select-menu-collectors channel-id])
        (swap! state assoc-in [:select-menu-collectors channel-id]
               (.. interaction
                   -channel
                   (createMessageComponentCollector #js
                                                    {:componentType
                                                     (.-SelectMenu discord/ComponentType)})))
        (let [collector (get-in @state [:select-menu-collectors channel-id])]
                              (.on collector "collect" handle-collector-event-select-menu!)))))

(defn handle-collector-event-user-select! [interaction]
  (println 'USER-SELECT-COLLECTOR)
  (let [command-name (.. interaction -message -interaction -commandName)]
    (case command-name
      ; "gg"(gg/interact! interaction)
      (println "OTHER"))
    ))

(defn init-collector-type-user-select [interaction]
  (let [channel-id (.. interaction -channel -id)]
    (when-not (get-in @state [:user-select-collectors channel-id])
        (swap! state assoc-in [:user-select-collectors channel-id]
               (.. interaction
                   -channel
                   (createMessageComponentCollector #js
                                                    {:componentType
                                                     (.-UserSelect discord/ComponentType)})))
        (let [collector (get-in @state [:user-select-collectors channel-id])]
                              (.on collector "collect" handle-collector-event-user-select!)))))

(defn handle-interaction [interaction]
  (init-collector-type-button interaction)
  (init-collector-type-select-menu interaction)
  (init-collector-type-user-select interaction)
  ; (.log js/console (.. interaction -message))
  (when (.isChatInputCommand interaction)
    (case (.-commandName interaction)
      "quote" (quote/interact! interaction)
      "make-teams"(make-teams/interact! interaction)
      "go"(go-command/interact! interaction)
      "gg"(gg/interact! interaction)
      (println "OTHER"))))

(defn on-channel-delete [channel]
  (swap! state update-in [:button-collectors] ; TODO add other collectors
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

