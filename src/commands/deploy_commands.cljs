(ns commands.deploy-commands
  (:require
   [config :refer [TOKEN CLIENT_ID]]
   ["discord.js" :as discord]
   [commands.quote :as quote]
   [commands.go :as go-command]
   [commands.make-teams :as make-teams]
   [commands.gg :as gg]
   [commands.get :as get-command]
   [commands.set :as set-command]
   [commands.top :as top]
   [commands.mem :as mem]
   [commands.coin :as coin]
   [commands.help :as help]))

(def commands [quote/builder make-teams/builder go-command/builder mem/builder coin/builder help/builder
               gg/builder get-command/builder set-command/builder top/builder])

(def rest-api (.setToken (discord/REST. #js {:version 10}) TOKEN))

(defn register-commands []
  (.. rest-api
      (put (.applicationCommands discord/Routes CLIENT_ID) #js {:body (clj->js commands)})
      (then #(println "Register commands SUCCESS!"))
      (catch #(println "ERROR register-commands deploy_commands" %))))
