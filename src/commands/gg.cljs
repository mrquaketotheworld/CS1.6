(ns commands.gg ; TODO idempotent
  (:require ["discord.js" :as discord]
            [cljs.core.async :refer [go]]
            [cljs.core.async.interop :refer-macros [<p!]]
            ["simple-elo-rating" :as elo]
            [db.connection :as db]
            [db.models.map-server :as map-server]
            [db.models.player :as player]
            [db.models.player-server-points :as player-server-points]))

(def builder
  (.. (discord/SlashCommandBuilder.)
      (setName "gg")
      (setDescription "Save the match result!")))

(def state (atom {:interactions {}}))

(def CYAN "#18FFFF")
(def WHITE "#FFFFFF")
(def RED "#d00a0a")
(def GREEN "#00ce21")

(defn sum-players-points [team-points]
  (reduce (fn [acc player]
            (+ acc (player "points"))) 0 team-points))

(defn get-interaction-id [interaction]
  (.. interaction -message -interaction -id))

(defn get-match-info [interaction-id]
  (get-in @state [:interactions interaction-id]))

(defn get-users [team]
  (map #(identity {:username (:username %) :user-id (:user-id %)}) team))

(defn generate-score-options []
  (map #(.. (discord/StringSelectMenuOptionBuilder.)
               (setLabel (str %))
               (setValue (str %))) (range 25)))

(defn generate-maps-options [maps]
  (map #(.. (discord/StringSelectMenuOptionBuilder.)
               (setLabel (% "map"))
               (setValue (% "map"))) maps))

(defn calculate-points-for-one-player [team-points-elo-diff players]
  (js/Number (.toFixed (/ team-points-elo-diff (count players)) 2)))

(defn handle-collector-event-button-save! [interaction]
  (go (try
        (let [match-info (get-match-info (get-interaction-id interaction))
              map-select (match-info "map-select")
              team1-score (js/Number (match-info "team1-score"))
              team2-score (js/Number (match-info "team2-score"))
              team1-users (get-users (match-info "team1"))
              team2-users (get-users (match-info "team2"))
              team1-ids (clj->js (map #(:user-id %) team1-users))
              team2-ids (clj->js (map #(:user-id %) team2-users))
              users (concat team1-users team2-users)
              server-id (.-guildId interaction)
              client (<p! (.connect db/pool))]
          (go (try
            (<p! (db/begin-transaction client))
            ; TODO validate qty players to 10
            (doseq [user users]
              (let [user-id (:user-id user)
                    username (:username user)]
                (<p! (player/insert-player-if-not-exists client user-id username))
                (let [player-server (.-rows (<p! (player-server-points/select-player-by-server
                                                   client user-id server-id)))]
                  (when (empty? player-server)
                    (<p! (player-server-points/insert-player
                           client user-id server-id))))))
            (let [team1-points
                    (js->clj
                      (.-rows
                       (<p! (player-server-points/select-players-points
                            client team1-ids server-id))))
                  team2-points
                    (js->clj
                      (.-rows
                       (<p! (player-server-points/select-players-points
                            client team2-ids server-id))))
                    team1-total-points (sum-players-points team1-points)
                    team2-total-points (sum-players-points team2-points)
                    elo-K-factor 160
                    elo-basic (.. (elo/Elo. elo-K-factor)
                                  (playerA team1-total-points)
                                  (playerB team2-total-points))
                    elo-result (.. (cond
                                     (> team1-score team2-score) (.setWinnerA elo-basic)
                                     (< team1-score team2-score) (.setWinnerB elo-basic)
                                     :else (.setDraw elo-basic))
                                   calculate
                                   getResults)
                    team1-points-elo-diff (- (first elo-result) team1-total-points)
                    team2-points-elo-diff (- (second elo-result) team2-total-points)
                    team1-points-to-every-player
                      (calculate-points-for-one-player team1-points-elo-diff team1-users)
                    team2-points-to-every-player
                      (calculate-points-for-one-player team2-points-elo-diff team2-users)]
              (doseq [team1-player-id team1-ids]
                (println team1-player-id team1-points-to-every-player)
                (<p! (player-server-points/update-player-points
                       client team1-player-id server-id team1-points-to-every-player))
                )
              (doseq [team2-player-id team2-ids]
                (println team2-player-id team2-points-to-every-player)
                (<p! (player-server-points/update-player-points
                       client team2-player-id server-id team2-points-to-every-player))
                )

              )

            (<p! (db/commit-transaction client))
          (catch js/Error e (do (println "ERROR handle-collector-event-button-save! gg" e)
                                (<p! (db/rollback-transaction client))))
          (finally (.release client)))))
        (catch js/Error e (do (println "ERROR handle-collector-event-button-save! gg" e))))))

(defn handle-collector-event-select-menu! [interaction]
  (let [interaction-id (get-interaction-id interaction)
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
                           (setStyle (.-Danger discord/ButtonStyle))))
        embed-title-who-team1 (.. (discord/EmbedBuilder.)
                          (setTitle "Who played for Team 1?")
                          (setColor CYAN))
        embed-title-who-team2 (.. (discord/EmbedBuilder.)
                          (setTitle "Who played for Team 2?")
                          (setColor CYAN))]
    (go (try
      (swap! state update-in [:interactions interaction-id] #(assoc % custom-id value))
      (case custom-id
        "team1-score"
          (<p! (.update interaction #js {:embeds #js [embed-title-who-team2]
                                         :components #js [team2-row]}))
        "team2-score"
          (let [match-info (get-match-info interaction-id)
                map-select (match-info "map-select")
                team1-score (js/Number (match-info "team1-score"))
                team2-score (js/Number (match-info "team2-score"))
                team1-usernames
                  (apply str (interpose ", " (map #(:username %) (match-info "team1"))))
                team2-usernames
                  (apply str (interpose ", " (map #(:username %) (match-info "team2"))))
                finish-message (str
                                  "If the data about match is CORRECT "
                                  "then click on the save button.\n"
                                  "If the data about match is WRONG "
                                  "then run `/gg` again.\n\n"
                              (discord/bold
                                (str ":warning: If you click on the save button, "
                                "then saved result of the match cannot be changed :warning:\n\n")))
                finish-message-map (.. (discord/EmbedBuilder.)
                          (setTitle map-select)
                          (setColor WHITE))
                finish-message-team1 (.. (discord/EmbedBuilder.)
                          (setTitle (str team1-score " | " team1-usernames))
                          (setColor (cond ; TODO move logic to function
                                      (< team1-score team2-score) RED
                                      (> team1-score team2-score) GREEN
                                      :else WHITE
                                      )))
                finish-message-team2 (.. (discord/EmbedBuilder.)
                          (setTitle (str team2-score " | " team2-usernames))
                          (setColor (cond
                                      (> team1-score team2-score) RED
                                      (< team1-score team2-score) GREEN
                                      :else WHITE
                                      )))]

            (<p! (.update interaction #js {:content finish-message
                                           :embeds #js [finish-message-map
                                                        finish-message-team1
                                                        finish-message-team2]
                                           :components #js [button-save-row]})))
        "map-select"
          (<p! (.update interaction #js { :embeds #js [embed-title-who-team1]
                                         :components #js [team1-row]})))
      (catch js/Error e (println "ERROR handle-collector-event-select-menu! gg" e))))
))

(defn handle-collector-event-user-select! [interaction]
  (let [interaction-id (get-interaction-id interaction)
        custom-id (.-customId interaction)
        users (reduce (fn [acc user-item]
                        (let [user-id (first user-item)
                              user (second user-item)]
                          (if nil ; TODO nil is for test
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
      (catch js/Error e (println "ERROR handle-collector-event-user-select! gg" e))))))

(defn interact! [interaction]
   ; FIXME unable use go <p! because of strange compilation errors
  (.catch (.then (map-server/select-maps (.-guildId interaction) "main")
    (fn [result]
      (js/setTimeout (fn []
        (swap! state assoc-in [:interactions (.-id interaction)] nil)
        (.deleteReply interaction)) 180000)
      (let [map-select (.. (discord/StringSelectMenuBuilder.)
                         (setCustomId "map-select")
                         (setPlaceholder "Map"))
          map-select-row (.addComponents (discord/ActionRowBuilder.) map-select)
          generated-options-maps (clj->js (generate-maps-options (js->clj (.-rows result))))
          embed-title (.. (discord/EmbedBuilder.)
                          (setTitle "What map did you play?")
                          (setColor CYAN))]
        (.apply map-select.addOptions map-select generated-options-maps)
        (.reply interaction #js {:embeds #js [embed-title]
                                 :components #js [map-select-row]
                                 :ephemeral true}))))
          (fn [e] (println "ERROR interact! gg" e))))
