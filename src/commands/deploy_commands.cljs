(ns commands.deploy-commands
  (:require
   [config :refer [TOKEN CLIENT_ID GUILD]]
   ["discord.js" :as discord]
   [commands.quote :as quote]
   [commands.go :as go-command]
   [commands.make-teams :as make-teams]
   [commands.gg :as gg]
   [commands.get :as get-command]))


(def commands [quote/builder make-teams/builder go-command/builder])
(def guild-commands [gg/builder get-command/builder])

(def rest-api (.setToken (discord/REST. #js {:version 10}) TOKEN))

(defn register-commands []
  (.. rest-api
      (put (.applicationCommands discord/Routes CLIENT_ID) #js {:body (clj->js commands)})
      (then #(println "Register commands SUCCESS!"))
      (catch #(println "ERROR register-commands deploy_commands" %))))

(defn register-guild-commands []
  (.. rest-api ; TODO change guildId
      (put (.applicationGuildCommands discord/Routes CLIENT_ID GUILD)
           #js {:body (clj->js guild-commands)})
      (then #(println "Register guild commands SUCCESS!"))
      (catch #(println "ERROR register-guild-commands deploy_commands" %))))
