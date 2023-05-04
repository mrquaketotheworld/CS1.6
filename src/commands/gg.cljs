(ns commands.gg
  (:require ["discord.js" :as discord]
            [cljs.core.async :refer [go]]
            [cljs.core.async.interop :refer-macros [<p!]]
            [db.models.map-server :as map-server]))

(def builder
  (.. (discord/SlashCommandBuilder.)
      (setName "gg")
      (setDescription "Save the match result!")))

(def state (atom {:interactions {}}))

(defn generate-score-options []
  (map #(.. (discord/StringSelectMenuOptionBuilder.)
               (setLabel (str %))
               (setValue (str %))) (range 25)))

(defn generate-maps-options [maps]
  (map #(.. (discord/StringSelectMenuOptionBuilder.)
               (setLabel (% "map"))
               (setValue (% "map"))) maps))

(defn handle-collector-event-select-menu! [interaction]
  (let [interaction-id (.. interaction -message -interaction -id)
        custom-id (.-customId interaction)
        value (first (.-values interaction))
        team1-row (.addComponents (discord/ActionRowBuilder.)
                     (.. (discord/UserSelectMenuBuilder.)
                     (setCustomId "team1")
                     (setPlaceholder "Team 1")
                     (setMinValues 2) ; TODO change to 5 players
                     (setMaxValues 2)))
        team2-row (.addComponents (discord/ActionRowBuilder.)
                     (.. (discord/UserSelectMenuBuilder.)
                     (setCustomId "team2")
                     (setPlaceholder "Team 2")
                     (setMinValues 2)
                     (setMaxValues 2)))
        button-save-row (.addComponents (discord/ActionRowBuilder.)
                           (.. (discord/ButtonBuilder.)
                           (setCustomId "button-save")
                           (setLabel "Save")
                           (setStyle (.-Primary discord/ButtonStyle))))]
    (swap! state update-in [:interactions interaction-id] #(assoc % custom-id value))
    (go (try
      (case custom-id
        "team1-score"
          (<p! (.update interaction #js {:content "Who played for Team 2?"
                                         :components #js [team2-row]}))
        "team2-score"
          (<p! (.update interaction #js {:content (str (discord/codeBlock "diff"
                                "-- The saved result of the match cannot be changed --")
                                "Please check the information you entered and click the button:")
                                         :components #js [button-save-row]}))
        "map-select"
          (<p! (.update interaction #js {:content "Who played for Team 1?"
                                         :components #js [team1-row]})))
      (catch js/Error e (println "ERROR 148 go" e))))
))

(defn handle-collector-event-user-select! [interaction]
  (let [interaction-id (.. interaction -message -interaction -id)
        custom-id (.-customId interaction)
        users (reduce (fn [acc user-item]
                        (let [user-id (first user-item)
                              user (second user-item)]
                          (if (.-bot user)
                            acc
                            (conj acc {:user-id user-id
                                       :username (.-username user)}))))
                   [] (.from js/Array (.-users interaction)))
        team1-score (.. (discord/StringSelectMenuBuilder.)
                           (setCustomId "team1-score")
                           (setPlaceholder "Team 1 Score"))
        team2-score (.. (discord/StringSelectMenuBuilder.)
                           (setCustomId "team2-score")
                           (setPlaceholder "Team 2 Score"))
        team1-score-row (.addComponents (discord/ActionRowBuilder.) team1-score)
        team2-score-row (.addComponents (discord/ActionRowBuilder.) team2-score)
        generated-options (clj->js (generate-score-options))
        ]
    (.apply team1-score.addOptions team1-score generated-options)
    (.apply team2-score.addOptions team2-score generated-options)
    (swap! state update-in [:interactions interaction-id] #(assoc % custom-id users))
    (println @state)
    (go (try
      (case custom-id
        "team2"
          (<p! (.update interaction #js {:content "What is the score of Team 2?"
                                         :components #js [team2-score-row]}))
        "team1"
          (<p! (.update interaction #js {:content "What is the score of Team 1?"
                              :components #js [team1-score-row]})))
      (catch js/Error e (println "ERROR 148 go" e))))))

(defn interact! [interaction]
   ; FIXME unable use go <p! because of strange compilation errors
  (.catch (.then (map-server/select-maps (.-guildId interaction) "main")
    (fn [result]
      (let [map-select (.. (discord/StringSelectMenuBuilder.)
                         (setCustomId "map-select")
                         (setPlaceholder "Map"))
          button-save (.addComponents (discord/ActionRowBuilder.)
                           (.. (discord/ButtonBuilder.)
                           (setCustomId "button-save")
                           (setLabel "Save")
                           (setStyle (.-Primary discord/ButtonStyle))))
          map-select-row (.addComponents (discord/ActionRowBuilder.) map-select)
          generated-options-maps (clj->js (generate-maps-options (js->clj (.-rows result))))]
        (.apply map-select.addOptions map-select generated-options-maps)
        (.reply interaction #js {:content "What map did you play?"
                                 :components #js [map-select-row]
                                 :ephemeral true}))))
          (fn [e] (println "ERROR 64 gg" e))))
