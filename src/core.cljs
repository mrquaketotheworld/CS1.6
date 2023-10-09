(ns core
  (:require ["discord.js" :as discord]
            [cljs.core.async :refer [go]]
            [cljs.core.async.interop :refer-macros [<p!]]
            [undici]
            [config :refer [TOKEN GUILD_ADMIN GUILD_SCORE GUILD_CHANNEL_SCORE GUILD_CHANNEL_BOT]]
            [commands.quote :as quote]
            [commands.make-teams :as make-teams]
            [commands.go :as go-command]
            [commands.deploy-commands :as deploy]
            [commands.gg :as gg]
            [commands.get :as get-command]
            [commands.set :as set-command]
            [commands.top :as top]
            [db.models.server :as server]
            [db.init-tables :as init-tables]
            [db.models.map-server :as map-server]))

(.setGlobalDispatcher undici (undici/Agent. #js {:connect {:timeout 60000}}))

(def client (discord/Client.
             #js {:intents #js [(.-Guilds discord/GatewayIntentBits)
                                (.-GuildVoiceStates discord/GatewayIntentBits)
                                (.-MessageContent discord/GatewayIntentBits)]}))

(def state (atom {:button-collectors {} :select-menu-collectors {} :user-select-collectors {}}))

(defn handle-collector-event-type-button! [interaction]
  (let [command-name (.. interaction -message -interaction -commandName)]
    (case command-name
      "go" (go-command/handle-collector-event-type-button! interaction)
      "gg" (gg/handle-collector-event-button! interaction))))

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
  (let [command-name (.. interaction -message -interaction -commandName)]
    (when (= command-name "gg") (gg/handle-collector-event-select-menu! interaction))))

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
  (let [command-name (.. interaction -message -interaction -commandName)]
    (when (= command-name "gg") (gg/handle-collector-event-user-select! interaction))))

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

(defn wrong-channel-command-reply [interaction channel]
  (.reply interaction #js {:content (str "Sorry, command only works in the <#" channel "> channel")
                           :ephemeral true}))

(defn handle-interaction [interaction]
  (go (try
        (let [server-id (.. interaction -guild -id)
              server-name (.. interaction -guild -name)
              server-with-maps (.-rows (<p! (map-server/check-server-with-maps-exists server-id)))
              user-roles (.. interaction -member -roles -cache)
              channel-id (.-channelId interaction)]
          (init-collector-type-button interaction)
          (init-collector-type-select-menu interaction)
          (init-collector-type-user-select interaction)
          (<p! (server/insert-server-if-not-exists server-id server-name))
          (when (empty? server-with-maps)
            (<p! (map-server/insert-default-maps server-id)))
          (when (.isChatInputCommand interaction)
            (case (.-commandName interaction)
              "quote" (quote/interact! interaction)
              "make-teams" (make-teams/interact! interaction)
              "go" (go-command/interact! interaction)
              "gg" (if (= channel-id GUILD_CHANNEL_SCORE)
                     (if (or (.has user-roles GUILD_ADMIN) (.has user-roles GUILD_SCORE))
                       (gg/interact! interaction)
                       (<p!
                        (.reply interaction #js
                                             {:content "Sorry, you do not have permissions to use this command"
                                              :ephemeral true})))
                     (<p! (wrong-channel-command-reply interaction GUILD_CHANNEL_SCORE)))
              "get" (if (= channel-id GUILD_CHANNEL_BOT)
                      (get-command/interact! interaction)
                      (<p! (wrong-channel-command-reply interaction GUILD_CHANNEL_BOT)))
              "set" (set-command/interact! interaction)
              "top" (if (= channel-id GUILD_CHANNEL_BOT)
                      (top/interact interaction)
                      (<p! (wrong-channel-command-reply interaction GUILD_CHANNEL_BOT))))))
        (catch js/Error e (println "ERROR handle-interaction core" e)))))

(.on client "ready" #(println "Ready!" (js/Date.)))
(.on client "interactionCreate" handle-interaction)
(.login client TOKEN)

(defn main [& args]
  (case (nth args 0)
    "register" (deploy/register-commands)
    "register-guild" (deploy/register-guild-commands)
    "init-tables" (init-tables/init-tables)
    (println "Start")))
