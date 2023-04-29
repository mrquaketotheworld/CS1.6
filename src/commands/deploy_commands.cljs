(ns commands.deploy-commands
  (:require
   [config :refer [TOKEN CLIENT_ID]]
   ["discord.js" :as discord]
   [commands.quote :as quote]
   [commands.make-teams :as make-teams]))


(def commands [quote/builder make-teams/builder])

(def rest-api (.setToken (discord/REST. #js {:version 10}) TOKEN))

(defn register-commands []
  (.. rest-api
      (put (.applicationCommands discord/Routes CLIENT_ID) #js {:body (clj->js commands)})
      (then #(println "Register commands SUCCESS!"))
      (catch #(println "ERROR in register-commands:" %))))

