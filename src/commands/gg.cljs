(ns commands.gg
  (:require ["discord.js" :as discord]
            [cljs.core.async :refer [go]]
            [cljs.core.async.interop :refer-macros [<p!]]
            [db.models.map-server :as map-server]))

(def builder
  (.. (discord/SlashCommandBuilder.)
      (setName "gg")
      (setDescription "Save the match result!")))

(defn generate-score-options []
  (map #(.. (discord/StringSelectMenuOptionBuilder.)
               (setLabel (str %))
               (setValue (str %))) (range 25)))

(defn generate-maps-options [maps]
  (map #(.. (discord/StringSelectMenuOptionBuilder.)
               (setLabel (% "map"))
               (setValue (% "map"))) maps))


(defn interact! [interaction]
   ; FIXME unable use go <p! because of strange compilation errors
  (.catch (.then (map-server/select-maps (.-guildId interaction) "main")
    (fn [result]
      (let [map-select (.. (discord/StringSelectMenuBuilder.)
                         (setCustomId "map-select")
                         (setPlaceholder "Map"))
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
          team2-score-row (.addComponents (discord/ActionRowBuilder.) team2-score)
          map-select-row (.addComponents (discord/ActionRowBuilder.) map-select)
          generated-options (clj->js (generate-score-options))
          generated-options-maps (clj->js (generate-maps-options (js->clj (.-rows result))))]
        (.apply map-select.addOptions map-select generated-options-maps)
        (.apply team1-score.addOptions team1-score generated-options)
        (.apply team2-score.addOptions team2-score generated-options)
        (go (try
              (<p! (.reply interaction #js {:components #js [map-select-row
                                                        team1
                                                        team1-score-row
                                                        team2
                                                        team2-score-row] :ephemeral true}))
              (<p! (.followUp interaction #js {
                            :content
                              (str (discord/codeBlock "diff"
                                "-- The saved result of the match cannot be changed --")
                                "Please check the information you entered and click the button:")
                             :components #js [button-save]
                             :ephemeral true}))
              (catch js/Error e (println "ERROR 148 go" e)))))))
          (fn [e] (println "ERROR 64 gg" e))))


