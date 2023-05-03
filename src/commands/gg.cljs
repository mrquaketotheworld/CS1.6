(ns commands.gg
  (:require ["discord.js" :as discord]
            [cljs.core.async :refer [go]]
            [cljs.core.async.interop :refer-macros [<p!]]))

(def builder
  (.. (discord/SlashCommandBuilder.)
      (setName "gg")
      (setDescription "Save the match result!")))

(defn generate-score-options []
  (map #(.. (discord/StringSelectMenuOptionBuilder.)
               (setLabel (str %))
               (setValue (str %))) (range 25)))

(defn interact! [interaction]
  (let [map-select (.addComponents (discord/ActionRowBuilder.)
                       (.. (discord/StringSelectMenuBuilder.)
                       (setCustomId "map-select")
                       (setPlaceholder "Map")
                       (addOptions (.. (discord/StringSelectMenuOptionBuilder.)
                                       (setLabel "Nuke") ; TODO add options from DB
                                       (setValue "Nuke")
                                       )
                                   (.. (discord/StringSelectMenuOptionBuilder.)
                                       (setLabel "Inferno")
                                       (setValue "Inferno")
                                       ))))
        team1 (.addComponents (discord/ActionRowBuilder.)
                   (.. (discord/UserSelectMenuBuilder.)
                   (setCustomId "team1")
                   (setPlaceholder "Team 1")
                   (setMinValues 2) ; TODO change to 5 players
                   (setMaxValues 2)))
        team1-score (.. (discord/StringSelectMenuBuilder.)
                         (setCustomId "team1-score")
                         (setPlaceholder "Team 1 Score"))
        team2 (.addComponents (discord/ActionRowBuilder.)
                   (.. (discord/UserSelectMenuBuilder.)
                   (setCustomId "team2")
                   (setPlaceholder "Team 2")
                   (setMinValues 2)
                   (setMaxValues 2)))
        team2-score (.. (discord/StringSelectMenuBuilder.)
                         (setCustomId "team2-score")
                         (setPlaceholder "Team 2 Score"))
        button-save (.addComponents (discord/ActionRowBuilder.)
                         (.. (discord/ButtonBuilder.)
                         (setCustomId "button-save")
                         (setLabel "Save")
                         (setStyle (.-Primary discord/ButtonStyle))))
        team1-score-row (.addComponents (discord/ActionRowBuilder.) team1-score)
        team2-score-row (.addComponents (discord/ActionRowBuilder.) team2-score)]
    (go (try
          (.apply team1-score.addOptions team1-score (clj->js (generate-score-options)))
          (.apply team2-score.addOptions team2-score (clj->js (generate-score-options)))
          (<p! (.reply interaction #js {:components #js [map-select
                                                         team1
                                                         team1-score-row
                                                         team2
                                                         team2-score-row] :ephemeral true}))
          (<p! (.followUp interaction
                          #js {
                              :content
                              (str (discord/codeBlock "diff"
                                "-- The saved result of the match cannot be changed --")
                                "Please check the information you entered and click the button:")
                               :components #js [button-save]
                               :ephemeral true}))
          (catch js/Error e (println "ERROR 70 gg" e))))))
