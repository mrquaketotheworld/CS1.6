(ns commands.deploy-commands
  (:require
   ["dotenv" :as dotenv]
   ["discord.js" :as discord]))

(def config ((js->clj (.config dotenv)) "parsed"))
(def TOKEN (config "TOKEN"))
(def CLIENT_ID (config "CLIENT_ID"))

(def commands [{:name "quote"
                :description "Show random player's quote!"}
               {:name "go"
                :description "Run map poll!"}
               {:name "go-extra"
                :description "Run extra map poll!"}
               {:name "go-fun"
                :description "Run fun map poll!"}
               {:name "make-teams"
                :description "Make random teams from voice channel!"}])

(defn prepare-commands [command]
  (let [name (:name command)
        description (:description command)]
    (.. (new discord/SlashCommandBuilder)
        (setName name)
        (setDescription description)
        (toJSON))))

(def commands-built (map prepare-commands commands))
(def rest-api (.setToken (new discord/REST #js {:version 10}) TOKEN))

(defn register-commands []
  (.. rest-api
      (put (.applicationCommands discord/Routes CLIENT_ID) #js {:body commands-built})
      (then #(.log js/console "Register commands SUCCESS!"))
      (catch #(.log js/console "ERROR:" %))))

