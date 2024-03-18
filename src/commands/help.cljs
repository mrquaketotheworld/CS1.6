(ns commands.help
  (:require ["discord.js" :as discord]
            [cljs.core.async :refer [go]]
            [cljs.core.async.interop :refer-macros [<p!]]))

(def builder
  (.. (discord/SlashCommandBuilder.)
      (setName "help")
      (setDescription "List of all commands")
      toJSON))

(defn interact! [interaction]
  (println "/help" (js/Date.))
  (let [commands [{:name "/coin" :desc "Toss a coin"}
                  {:name "/get" :desc "Get user stats"}
                  {:name "/gg" :desc "Save the match result"}
                  {:name "/go" :desc "Run map poll"}
                  {:name "/help" :desc "List of all commands"}
                  {:name "/make-teams" :desc "Make random teams from voice channel"}
                  {:name "/mem" :desc "Create a mem"}
                  {:name "/quote" :desc "Show random player's quote"}
                  {:name "/set" :desc "Set some fields in your stats card"}
                  {:name "/top" :desc "Show top 10 players"}]
        commands-string (reduce (fn [acc command]
                                  (str acc "`" (:name command) "` " (:desc command)
                                       "\n")) "" commands)]

    (go (try
          (<p! (.reply interaction #js {:content commands-string}))
          (catch js/Error e (println "ERROR help" e))))))
