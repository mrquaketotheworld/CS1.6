(ns commands.gg ; TODO idempotent
  (:require ["discord.js" :as discord]
            [cljs.core.async :refer [go]]
            [cljs.core.async.interop :refer-macros [<p!]]
            [db.models.map-server :as map-server]))

(def builder
  (.. (discord/SlashCommandBuilder.)
      (setName "gg")
      (setDescription "Save the match result!")))

(def state (atom {:interactions {}}))

(def CYAN "#18FFFF")

(defn generate-score-options []
  (map #(.. (discord/StringSelectMenuOptionBuilder.)
               (setLabel (str %))
               (setValue (str %))) (range 25)))

(defn generate-maps-options [maps]
  (map #(.. (discord/StringSelectMenuOptionBuilder.)
               (setLabel (% "map"))
               (setValue (% "map"))) maps))

(defn handle-collector-event-button-save [interaction]
  )

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
                           (setStyle (.-Primary discord/ButtonStyle))))
        embed-title-who-team1 (.. (discord/EmbedBuilder.)
                          (setTitle "Who played for Team 1?")
                          (setColor CYAN))
        embed-title-who-team2 (.. (discord/EmbedBuilder.)
                          (setTitle "Who played for Team 2?")
                          (setColor CYAN))]
    (swap! state update-in [:interactions interaction-id] #(assoc % custom-id value))
    (go (try
      (case custom-id
        "team1-score"
          (<p! (.update interaction #js {:embeds #js [embed-title-who-team2]
                                         :components #js [team2-row]}))
        "team2-score"
          (let [match-info (get-in @state [:interactions interaction-id])
                map-select (match-info "map-select")
                team1-score (match-info "team1-score")
                team2-score (match-info "team2-score")
                team1-usernames
                  (apply str (interpose ", " (map #(:username %) (match-info "team1"))))
                team2-usernames
                  (apply str (interpose ", " (map #(:username %) (match-info "team2"))))
                finish-message (str map-select "\n"
                                  team1-score " " team1-usernames "\n"
                                  team2-score " " team2-usernames "\n\n"
                                  "Is the data about match is correct?\n"
                                  "If correct then click on the save button\n"
                                  "or do nothing\n"
                                  "or run ```/gg``` again\n"
                                  (discord/codeBlock "diff"
                                  "-- The saved result of the match cannot be changed --"))]

            (<p! (.update interaction #js {:content finish-message
                                         :components #js [button-save-row]})))
        "map-select"
          (<p! (.update interaction #js { :embeds #js [embed-title-who-team1]
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
        embed-title-what-score-team1 (.. (discord/EmbedBuilder.)
                          (setTitle "What is the score of Team 1?")
                          (setColor CYAN))
        embed-title-what-score-team2 (.. (discord/EmbedBuilder.)
                          (setTitle "What is the score of Team 2?")
                          (setColor CYAN))
        team1-score-row (.addComponents (discord/ActionRowBuilder.) team1-score)
        team2-score-row (.addComponents (discord/ActionRowBuilder.) team2-score)
        generated-options (clj->js (generate-score-options))]
    (go (try
      (if (< (count users) 2)
        (<p! (.reply interaction #js {:content "Only user data can be saved, not bots"
                                      :ephemeral true}))
        (do
          (.apply team1-score.addOptions team1-score generated-options)
          (.apply team2-score.addOptions team2-score generated-options)
          (swap! state update-in [:interactions interaction-id] #(assoc % custom-id users))
          (case custom-id
            "team2"
              (let [match-info (get-in @state [:interactions interaction-id])
                    team1-match-info (match-info "team1")
                    team2-match-info (match-info "team2")
                    team1-users-set (set (map #(:user-id %) team1-match-info))
                    team2-users-set (set (map #(:user-id %) team2-match-info))]
                ;TODO add validation
                (println (= (count (clojure.set/intersection team1-users-set team2-users-set)) 0))
                (<p! (.update interaction #js {:embeds #js [embed-title-what-score-team2]
                                              :components #js [team2-score-row]})))
            "team1"
              (<p! (.update interaction #js { :embeds #js [embed-title-what-score-team1]
                                             :components #js [team1-score-row]})))))
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
                           (setStyle (.-Success discord/ButtonStyle))))
          map-select-row (.addComponents (discord/ActionRowBuilder.) map-select)
          generated-options-maps (clj->js (generate-maps-options (js->clj (.-rows result))))
          embed-title (.. (discord/EmbedBuilder.)
                          (setTitle "What map did you play?")
                          (setColor CYAN))
          ]
        (.apply map-select.addOptions map-select generated-options-maps)
        (.reply interaction #js {:embeds #js [embed-title]
                                 :components #js [map-select-row]
                                 :ephemeral true}))))
          (fn [e] (println "ERROR 64 gg" e))))

(apply str (interpose ", " '("hello" "world")))

5
