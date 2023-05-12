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

(defn create-embed [title color]
  (.. (discord/EmbedBuilder.)
      (setTitle title)
      (setColor color)))

(defn create-embed-question [title]
  (create-embed title CYAN))

(defn create-team-embed [main-team-score opponent-teams-score main-team-usernames]
  (create-embed (str main-team-score (correct-score-space main-team-score) main-team-usernames)
                (cond
                  (< main-team-score opponent-teams-score) RED
                  (> main-team-score opponent-teams-score) GREEN
                  :else WHITE)))

(defn create-map-embed [map-select]
  (create-embed map-select LIGHT-BLACK))

(defn create-button [custom-id label button-style]
  (.. (discord/ButtonBuilder.)
      (setCustomId custom-id)
      (setLabel label)
      (setStyle button-style)))

(defn create-row [& components]
  (let [row-builder (discord/ActionRowBuilder.)]
    (.apply row-builder.addComponents row-builder (clj->js components))))

(def button-cancel (create-button "button-cancel" "Cancel" (.-Primary discord/ButtonStyle)))
(def button-save (create-button "button-save" "Save" (.-Danger discord/ButtonStyle)))
(def button-cancel-row (create-row button-cancel))
(def button-cancel-save-row (create-row button-cancel button-save))

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

(defn create-user-select [custom-id placeholder]
  (.. (discord/UserSelectMenuBuilder.)
      (setCustomId custom-id)
      (setPlaceholder placeholder)
      (setMinValues TEAM-NUMBER)
      (setMaxValues TEAM-NUMBER)))

(defn create-string-select [custom-id placeholder]
  (.. (discord/StringSelectMenuBuilder.)
      (setCustomId custom-id)
      (setPlaceholder placeholder)))

(def team1-row (create-row (create-user-select "team1" "Team 1")))
(def team2-row (create-row (create-user-select "team2" "Team 2")))

(defn get-only-users [interaction]
  (reduce (fn [acc user-item]
            (let [user-id (first user-item)
                  user (second user-item)]
              (if nil ; TODO
                acc
                (conj acc {:user-id user-id
                           :username (.-username user)}))))
          [] (.from js/Array (.-users interaction))))

(defn is-user-owner-of-this [interaction]
  (when-let [interaction-form (get-interaction-form (get-user-id interaction))]
    (= (.. interaction -message -interaction -id) (.-id (:interaction interaction-form)))))

(defn reply-wrong-interaction [interaction]
  (.catch (.reply interaction #js {:content "Oops...this is not your form" :ephemeral true})
          #(println "ERROR reply-wrong-interaction gg" %)))

(defn handle-collector-event-button! [interaction]
  (if (is-user-owner-of-this interaction)
    (go (try
          (let [user-id (get-user-id interaction)
                custom-id (.-customId interaction)
                match-info (get-interaction-form user-id)
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
            (case custom-id
              "button-cancel"
              (do
                (<p! (.deleteReply (:interaction match-info)))
                (reset-interaction-in-state user-id))
              "button-save"
              (go (try
                    (<p! (db/begin-transaction client))
                    (doseq [user users]
                      (let [user-id-iter (:user-id user)
                            username (:username user)]
                        (<p! (player/insert-player-if-not-exists client user-id-iter username))
                        (let [player-server (.-rows (<p!
                                                      (player-server-points/select-player-by-server
                                                          client user-id-iter server-id)))]
                          (when (empty? player-server)
                            (<p! (player-server-points/insert-player
                                  client user-id-iter server-id))))))
                    (let [team1-points
                          (db-utils/get-formatted-rows
                            (<p! (player-server-points/select-players-points
                                   client (clj->js team1-ids) server-id)))
                          team2-points
                          (db-utils/get-formatted-rows
                            (<p! (player-server-points/select-players-points
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
                      (let [match-id ((db-utils/get-first-formatted-row
                                        (<p! (match/insert-match client map-select team1-score
                                                       team2-score team1-id team2-id))) "id")]
                        (reset-interaction-in-state user-id)
                        (<p! (.update interaction
                                      #js {:content (str "Match ID: " match-id)
                                           :embeds #js [(create-map-embed map-select)
                                                        (create-team-embed team1-score team2-score
                                                                           team1-usernames)
                                                        (create-team-embed team2-score team1-score
                                                                           team2-usernames)]
                                           :components #js []}))))

                    (<p! (db/commit-transaction client))
                    (catch js/Error e (do (println "ERROR handle-collector-event-button! gg" e)
                                          (<p! (db/rollback-transaction client))))
                    (finally (.release client))))))
          (catch js/Error e (println "ERROR handle-collector-event-button! gg" e))))
    (reply-wrong-interaction interaction)))

(defn handle-collector-event-select-menu! [interaction]
  (if (is-user-owner-of-this interaction)
    (let [user-id (get-user-id interaction)
          custom-id (.-customId interaction)
          value (first (.-values interaction))
          embed-title-who-team1 (create-embed-question "Who played for Team 1?")
          embed-title-who-team2 (create-embed-question "Who played for Team 2?")]
      (go (try
            (update-interaction-in-state user-id custom-id value)
            (case custom-id
              "team1-score"
              (<p! (.update interaction #js {:embeds #js [embed-title-who-team2]
                                             :components #js [team2-row button-cancel-row]}))
              "team2-score"
              (let [match-info (get-interaction-form user-id)
                    map-select (match-info "map-select")
                    team1-score (js/Number (match-info "team1-score"))
                    team2-score (js/Number (match-info "team2-score"))
                    team1-usernames (create-user-list-string (match-info "team1"))
                    team2-usernames (create-user-list-string (match-info "team2"))
                    finish-message  (discord/bold
                                     (str "\n:warning:WARNING:warning:\n"
                                          "Please check the data you entered carefully.\n"
                                          "If you click on the save button, "
                                  "then saved result of the match and stats cannot be changed.\n"))
                    finish-message-map (create-map-embed map-select)
                    finish-message-team1 (create-team-embed team1-score team2-score
                                                            team1-usernames)
                    finish-message-team2 (create-team-embed team2-score team1-score
                                                            team2-usernames)]
                (<p! (.update interaction #js {:content finish-message
                                               :embeds #js [finish-message-map
                                                            finish-message-team1
                                                            finish-message-team2]
                                               :components #js [button-cancel-save-row]})))
              "map-select"
              (<p! (.update interaction #js {:embeds #js [embed-title-who-team1]
                                             :components #js [team1-row button-cancel-row]})))
            (catch js/Error e (println "ERROR handle-collector-event-select-menu! gg" e)))))
    (reply-wrong-interaction interaction)))

(defn handle-collector-event-user-select! [interaction]
  (if (is-user-owner-of-this interaction)
    (let [user-id (get-user-id interaction)
          custom-id (.-customId interaction)
          users (get-only-users interaction)
          team1-score-string-select (create-string-select "team1-score" "Team 1 Score")
          team2-score-string-select (create-string-select "team2-score" "Team 2 Score")
          embed-title-what-score-team1 (create-embed-question "What is the score of Team 1?")
          embed-title-what-score-team2 (create-embed-question "What is the score of Team 2?")
          team1-score-row (create-row team1-score-string-select)
          team2-score-row (create-row team2-score-string-select)
          generated-options (clj->js (generate-score-options))]
      (go (try
            (if (< (count users) TEAM-NUMBER)
              (<p! (.reply interaction #js {:content "Only user data can be saved, not bots"
                                            :ephemeral true}))
              (do
                (.apply team1-score-string-select.addOptions team1-score-string-select
                        generated-options)
                (.apply team2-score-string-select.addOptions team2-score-string-select
                        generated-options)
                (update-interaction-in-state user-id custom-id users)
                (case custom-id
                  "team2"
                  (let [match-info (get-interaction-form user-id)
                        team1-match-info (match-info "team1")
                        team2-match-info (match-info "team2")
                        team1-users-set (set (map #(:user-id %) team1-match-info))
                        team2-users-set (set (map #(:user-id %) team2-match-info))]
                    (if (= (count (clojure.set/intersection team1-users-set team2-users-set)) 0)
                      (<p! (.update interaction #js
                                                 {:embeds #js [embed-title-what-score-team2]
                                                  :components #js [team2-score-row
                                                                   button-cancel-row]}))
                      (<p! (.reply interaction #js
                                   {:content "One player cannot play on two teams at the same time"
                                    :ephemeral true}))))
                  "team1"
                  (<p! (.update interaction
                                #js {:embeds #js [embed-title-what-score-team1]
                                     :components #js [team1-score-row button-cancel-row]})))))
            (catch js/Error e (println "ERROR handle-collector-event-user-select! gg" e)))))
    (reply-wrong-interaction interaction)))

(defn interact! [interaction]
  (let [user-id (get-user-id interaction)
        map-select (create-string-select "map-select" "Map")
        map-select-row (create-row map-select)
        embed-title (create-embed-question "What map did you play?")]
    (go
      (try
        (let [maps (db-utils/get-formatted-rows
                    (<p! (map-server/select-maps (.-guildId interaction) "main")))
              generated-options-maps (clj->js (generate-maps-options maps))
              pending-interaction (get-interaction-form user-id)]
          (try (when pending-interaction
                 (<p! (.deleteReply (:interaction pending-interaction))))
               (catch js/Error e (println "ERROR interact!:deleteReply gg" e)))
          (reset-interaction-in-state user-id)
          (update-interaction-in-state user-id :interaction interaction)
          (.apply map-select.addOptions map-select generated-options-maps)
          (<p! (.reply interaction #js {:embeds #js [embed-title]
                                        :components #js [map-select-row button-cancel-row]})))
        (catch js/Error e (println "ERROR interact! gg" e))))))
