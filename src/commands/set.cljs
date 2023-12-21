(ns commands.set
  (:require ["discord.js" :as discord]
            [cljs.core.async :refer [go]]
            [cljs.core.async.interop :refer-macros [<p!]]
            [db.models.player :as player]))

(def builder
  (.. (discord/SlashCommandBuilder.)
      (setName "set")
      (setDescription "Set some fields in your stats card")
      (addStringOption (fn [option]
                         (.. option
                             (setName "team")
                             (setDescription "Enter your team, 1-13 characters")
                             (setMinLength 1)
                             (setMaxLength 13))))
      toJSON))

(defn interact! [interaction]
  (println "/set " (js/Date.))
  (go (try
        (let [team (.. interaction -options (getString "team"))
              user-id (.. interaction -user -id)]
          (if team
            (do
              (<p! (player/update-player user-id "tag" team))
              (<p! (.reply interaction #js {:content (str "Your new team is " team)
                                            :ephemeral true})))
            (<p! (.reply interaction #js {:content "You have not changed any settings"
                                          :ephemeral true}))))
        (catch js/Error e (println "ERROR interact! set" e)))))
