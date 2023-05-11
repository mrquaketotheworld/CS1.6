(ns commands.gg
  (:require ["discord.js" :as discord]
            ["simple-elo-rating" :as elo]
            [cljs.core.async :refer [go]]
            [cljs.core.async.interop :refer-macros [<p!]]
            [clojure.set]
            [db.connection :as db]
            [db.models.map-server :as map-server]
            [db.models.player :as player]
            [db.models.player-server-points :as player-server-points]
            [db.models.team :as team]
            [db.models.player-team-server :as player-team-server]
            [db.models.match :as match]
            [shared.db-utils :as db-utils]
            [shared.constants :refer
             [WHITE GREEN LIGHT-BLACK CYAN RED]]))

(def builder
  (.. (discord/SlashCommandBuilder.)
      (setName "gg")
      (setDescription "Save the match result!")))

(def state (atom {:interactions {}}))

(def TEAM-NUMBER 5)

(defn update-interaction-in-state [user-id k v]
  (swap! state update-in [:interactions user-id] #(assoc % k v)))

(defn reset-interaction-in-state [user-id]
  (swap! state update :interactions dissoc user-id))

(defn correct-score-space [score]
  (if (> (count (str score)) 1) "   " "     "))

(defn create-team-embed [main-team-score opponent-teams-score main-team-usernames]
  (.. (discord/EmbedBuilder.)
      (setTitle (str main-team-score (correct-score-space main-team-score) main-team-usernames))
      (setColor (cond
                  (< main-team-score opponent-teams-score) RED
                  (> main-team-score opponent-teams-score) GREEN
                  :else WHITE))))

(defn create-map-embed [map-select]
  (.. (discord/EmbedBuilder.)
      (setTitle map-select)
      (setColor LIGHT-BLACK)))

(defn sum-players-points [team-points]
  (reduce (fn [acc player]
            (+ acc (player "points"))) 0 team-points))

(defn get-user-id [interaction]
  (.. interaction -user -id))

(defn get-interaction-form [user-id]
  (get-in @state [:interactions user-id]))

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
  (/ team-points-elo-diff (count players)))

(defn create-user-list-string [team-info]
  (apply str (interpose ", " (map #(:username %) team-info))))

(defn handle-collector-event-button-save! [interaction]
  (go (try
        (let [match-info (get-interaction-form (get-user-id interaction))
              team1-usernames (create-user-list-string (match-info "team1"))
              team2-usernames (create-user-list-string (match-info "team2"))
              map-select (match-info "map-select")
              team1-score (js/Number (match-info "team1-score"))
              team2-score (js/Number (match-info "team2-score"))
              team1-users (get-users (match-info "team1"))
              team2-users (get-users (match-info "team2"))
              team1-ids (map #(:user-id %) team1-users)
              team2-ids (map #(:user-id %) team2-users)
              users (concat team1-users team2-users)
              server-id (.-guildId interaction)
              client (<p! (.connect db/pool))]
          (go (try
                (<p! (db/begin-transaction client))
                (doseq [user users]
                  (let [user-id (:user-id user)
                        username (:username user)]
                    (<p! (player/insert-player-if-not-exists client user-id username))
                    (let [player-server (.-rows (<p! (player-server-points/select-player-by-server
                                                      client user-id server-id)))]
                      (when (empty? player-server)
                        (<p! (player-server-points/insert-player client user-id server-id))))))
                (let [team1-points
                      (db-utils/get-formatted-rows (<p! (player-server-points/select-players-points
                                                         client (clj->js team1-ids) server-id)))
                      team2-points
                      (db-utils/get-formatted-rows (<p! (player-server-points/select-players-points
                                                         client (clj->js team2-ids) server-id)))
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
                      (calculate-points-for-one-player team2-points-elo-diff team2-users)
                      team1-id ((db-utils/get-first-formatted-row
                                 (<p! (team/insert-generate-team-id client))) "id")
                      team2-id ((db-utils/get-first-formatted-row
                                 (<p! (team/insert-generate-team-id client))) "id")]
                  (doseq [team1-player-id team1-ids]
                    (<p! (player-server-points/update-player-points
                          client team1-player-id server-id team1-points-to-every-player)))
                  (doseq [team2-player-id team2-ids]
                    (<p! (player-server-points/update-player-points
                          client team2-player-id server-id team2-points-to-every-player)))
                  (<p! (player-team-server/insert-team client
                                                       (nth team1-ids 0)
                                                       (nth team1-ids 1)
                                                       (nth team1-ids 2)
                                                       (nth team1-ids 3)
                                                       (nth team1-ids 4)
                                                       team1-id
                                                       server-id))
                  (<p! (player-team-server/insert-team client
                                                       (nth team2-ids 0)
                                                       (nth team2-ids 1)
                                                       (nth team2-ids 2)
                                                       (nth team2-ids 3)
                                                       (nth team2-ids 4)
                                                       team2-id
                                                       server-id))
                  (<p!
                  (match/insert-match client map-select team1-score team2-score team1-id team2-id))
                  (<p! (.delete (:init-message match-info)))
                  (reset-interaction-in-state (get-user-id interaction))
                  (<p! (.update interaction #js {:embeds #js []
                                                 :content "Match successfully saved!"
                                                 :components #js []}))
                  (<p! (.followUp interaction
                                  #js {:content
                                       (str "Saved by <@" (.. interaction -user -id) ">")
                                       :embeds #js [(create-map-embed map-select)
                                      (create-team-embed team1-score team2-score team1-usernames)
                                  (create-team-embed team2-score team1-score team2-usernames)]})))

                (<p! (db/commit-transaction client))
                (catch js/Error e (do (println "ERROR handle-collector-event-button-save! gg" e)
                                      (<p! (db/rollback-transaction client))))
                (finally (.release client)))))
        (catch js/Error e (println "ERROR handle-collector-event-button-save! gg" e)))))

(defn handle-collector-event-select-menu! [interaction]
  (let [user-id (get-user-id interaction)
        custom-id (.-customId interaction)
        value (first (.-values interaction))
        team1-row (.addComponents (discord/ActionRowBuilder.)
                                  (.. (discord/UserSelectMenuBuilder.)
                                      (setCustomId "team1")
                                      (setPlaceholder "Team 1")
                                      (setMinValues TEAM-NUMBER)
                                      (setMaxValues TEAM-NUMBER)))
        team2-row (.addComponents (discord/ActionRowBuilder.)
                                  (.. (discord/UserSelectMenuBuilder.)
                                      (setCustomId "team2")
                                      (setPlaceholder "Team 2")
                                      (setMinValues TEAM-NUMBER)
                                      (setMaxValues TEAM-NUMBER)))
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
          (update-interaction-in-state user-id custom-id value)
          (case custom-id
            "team1-score"
            (<p! (.update interaction #js {:embeds #js [embed-title-who-team2]
                                           :components #js [team2-row]}))
            "team2-score"
            (let [match-info (get-interaction-form user-id)
                  map-select (match-info "map-select")
                  team1-score (js/Number (match-info "team1-score"))
                  team2-score (js/Number (match-info "team2-score"))
                  team1-usernames (create-user-list-string (match-info "team1"))
                  team2-usernames (create-user-list-string (match-info "team2"))
                  finish-message (str
                                  "If the data about match is **CORRECT** "
                                  "then click on the save button.\n"
                                  "If the data about match is **WRONG** "
                                  "then run `/gg` again.\n\n"
                                  (discord/bold
                                   (str ":warning: If you click on the save button, "
                                "then saved result of the match cannot be changed :warning:\n\n")))
                  finish-message-map (create-map-embed map-select)
                  finish-message-team1 (create-team-embed team1-score team2-score team1-usernames)
                  finish-message-team2 (create-team-embed team2-score team1-score team2-usernames)]
              (<p! (.update interaction #js {:content finish-message
                                             :embeds #js [finish-message-map
                                                          finish-message-team1
                                                          finish-message-team2]
                                             :components #js [button-save-row]})))
            "map-select"
            (<p! (.update interaction #js {:embeds #js [embed-title-who-team1]
                                           :components #js [team1-row]})))
          (catch js/Error e (println "ERROR handle-collector-event-select-menu! gg" e))))))

(defn handle-collector-event-user-select! [interaction]
  (let [user-id (get-user-id interaction)
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
          (if (< (count users) TEAM-NUMBER)
            (<p! (.reply interaction #js {:content "Only user data can be saved, not bots"
                                          :ephemeral true}))
            (do
              (.apply team1-score.addOptions team1-score generated-options)
              (.apply team2-score.addOptions team2-score generated-options)
              (update-interaction-in-state user-id custom-id users)
              (case custom-id
                "team2"
                (let [match-info (get-interaction-form user-id)
                      team1-match-info (match-info "team1")
                      team2-match-info (match-info "team2")
                      team1-users-set (set (map #(:user-id %) team1-match-info))
                      team2-users-set (set (map #(:user-id %) team2-match-info))]
                  (if (= (count (clojure.set/intersection team1-users-set team2-users-set)) 0)
                    (<p! (.update interaction #js {:embeds #js [embed-title-what-score-team2]
                                                   :components #js [team2-score-row]}))
                    (<p! (.reply interaction #js {:content
                                             "One player cannot play on two teams at the same time"
                                                  :ephemeral true}))))
                "team1"
                (<p! (.update interaction #js {:embeds #js [embed-title-what-score-team1]
                                               :components #js [team1-score-row]})))))
          (catch js/Error e (println "ERROR handle-collector-event-user-select! gg" e))))))

(defn interact! [interaction]
  (let [user-id (get-user-id interaction)
        map-select (.. (discord/StringSelectMenuBuilder.)
                       (setCustomId "map-select")
                       (setPlaceholder "Map"))
        map-select-row (.addComponents (discord/ActionRowBuilder.) map-select)
        embed-title (.. (discord/EmbedBuilder.)
                        (setTitle "What map did you play?")
                        (setColor CYAN))]
    (go
      (try
        (let [maps (db-utils/get-formatted-rows
                    (<p! (map-server/select-maps (.-guildId interaction) "main")))
              generated-options-maps (clj->js (generate-maps-options maps))
              pending-interaction (get-interaction-form user-id)
              channel (.. interaction -channel)
              init-message (<p! (.send channel (str ":warning: <@" user-id "> is writing "
                                       "the result of the match now :warning:")))]
          (try (when pending-interaction
                 (<p! (.delete (:init-message pending-interaction)))
                 (<p! (.deleteReply (:interaction pending-interaction))))
               (catch js/Error e (println "ERROR interact!:deleteReply gg" e)))
          (reset-interaction-in-state user-id)
          (update-interaction-in-state user-id :interaction interaction)
          (update-interaction-in-state user-id :init-message init-message)
          (.apply map-select.addOptions map-select generated-options-maps)
          (<p! (.reply interaction #js {:content (str "If you make a mistake filling out the form, "
                                                  "run the `/gg` command again")
                                    :embeds #js [embed-title]
                                    :components #js [map-select-row]
                                    :ephemeral true})))
        (catch js/Error e (println "ERROR interact! gg" e))))))
