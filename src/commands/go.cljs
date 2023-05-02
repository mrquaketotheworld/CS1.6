(ns commands.go
  (:require ["discord.js" :as discord]
            [cljs.core.async :refer [go]]
            [cljs.core.async.interop :refer-macros [<p!]]
            [db.connection :as db]
            [db.models.server :as server]
            [db.models.map-server :as map-server]))
; TODO remove ^js/Object
(def builder
  (.. (discord/SlashCommandBuilder.)
      (setName "go")
      (setDescription "Run map poll!")
      (addStringOption (fn [option]
                         (.. option
                             (setName "mapmode")
                             (setDescription "Choose map mode")
                             (addChoices #js {:name "extra" :value "extra"}
                                         #js {:name "fun" :value "fun"}))))
      toJSON))

(def state (atom {:collectors {}
                  :interactions {}
                  }))

(def MAP-QUESTION ":triangular_flag_on_post: **VOTE FOR THE MAP PLEASE**, **1 MINUTE TO VOTE**")

(defn get-maps [interaction-id]
  (get-in @state [:interactions interaction-id :maps]))

(defn create-row [components]
  (.addComponents (discord/ActionRowBuilder.) (clj->js components)))

(defn create-button [map-name label is-disabled]
  (.. (discord/ButtonBuilder.)
      (setCustomId map-name)
      (setLabel label)
      (setStyle (.-Primary discord/ButtonStyle))
      (setDisabled is-disabled)))

(defn create-buttons [interaction-id]
  (clj->js (reduce (fn [acc map-group]
             (conj acc (create-row (into [] (map (fn [map-item]
                                                   (create-button
                                                     (:map-name map-item)
                                                     (:map-name map-item) ; TODO
                                                     (:is-disabled map-item))) map-group)))))
           [] (partition-all 4 (get-maps interaction-id)))))

(defn get-voted-users [maps]
  (sort (fn [a b] (- (:timestamp a) (:timestamp b)))
        (reduce (fn [acc map-item] (concat acc (:voted-users map-item))) [] maps)))

(defn calculate-votes []
  "FINISH")

(defn create-users-list [maps]
  (let [users-list-string
        (reduce (fn [acc voted-user]
                  (str acc ":jigsaw: " (discord/bold (:username voted-user))": "
                       (:map-name voted-user) "\n"))
                "" (get-voted-users maps))]
    (if (empty? users-list-string) "" users-list-string)))

(defn create-content [interaction-id]
  (let [maps (get-maps interaction-id)
        title (if (:is-disabled (first maps)) (calculate-votes) MAP-QUESTION)
        users-list (create-users-list maps)]
    (str title "\n" users-list)))

(defn format-maps [maps]
  (map (fn [map-item]
         {:map-name (map-item "map")
          :voted-users []
          :is-disabled false}) maps))

(defn convert-users-to-string [users]
  (reduce (fn [acc user]
            (str acc "<@" (.. user -user -id) ">")) "" users))

(defn wrong-vote-reply [interaction callee-voice-channel-id username]
  (.reply interaction #js {:content
                         (str "This is the voting of <#"
                              callee-voice-channel-id "> voice channel, " username)
                         :ephemeral true}))

(defn find-user-in-maps [interaction-id user-id]
  (reduce (fn [acc map-item]
            (let [user-found (->> (:voted-users map-item)
                                  (filter #(= (:user-id %) user-id))
                                  (first))]
              (if user-found
                user-found
                acc))) nil (get-maps interaction-id)))

(defn save-user-in-maps [interaction-id user-id username map-name]
  (swap! state update-in [:interactions interaction-id :maps]
                 #(map (fn [map-item] (if (= (:map-name map-item) map-name)
                                      (update map-item :voted-users conj {:user-id user-id
                                                                          :username username
                                                                          :map-name map-name
                                                                          :timestamp (.now js/Date)
                                                                          }) map-item)) %)))

(defn create-reply [interaction-id]
  #js {:content (create-content interaction-id)
       :components (create-buttons interaction-id)})

(defn handle-collector-event! [event]
  (let [event-interaction-id (.. event -message -interaction -id)
        map-name (.-customId event)
        user-id (.. event -user -id)
        username (.. event -user -username)
        voice-channel (.. event -member -voice -channel)
        callee-voice-channel-id (get-in @state
                                    [:interactions event-interaction-id :callee-voice-channel-id])]
    ; (println 'event-interaction-id event-interaction-id)
    ; (println 'map-name map-name)
    ; (println 'user-id user-id)
    ; (println 'username username)
     ; (println 'voice-channel voice-channel)
    ; (println 'callee-voice-channel-id callee-voice-channel-id)

    (if voice-channel
      (if (= callee-voice-channel-id (.-id voice-channel))
        (let [user-found (find-user-in-maps event-interaction-id user-id)]
          (go (try
                (if user-found
                  (<p! (.reply event #js {:content (str "You have already voted for "
                                                        (:map-name user-found) ", " username)
                                          :ephemeral true}))
                  (do
                    (save-user-in-maps event-interaction-id user-id username map-name)
                    (<p! (.update event (create-reply event-interaction-id)))))
                (catch js/Error e (println e)))))
        (wrong-vote-reply event callee-voice-channel-id username))
      (wrong-vote-reply event callee-voice-channel-id username))))

(defn init-interaction [interaction maps]
  (swap! state update :interactions
         #(assoc % (.-id interaction) { :maps maps
                                   :callee-voice-channel-id (.. interaction
                                                                -member
                                                                -voice
                                                                -channel
                                                                -id)})))

(defn init-collector [interaction channel-id]
  (when-not (get-in @state [:collectors channel-id])
    (swap! state assoc-in [:collectors channel-id]
           (.. interaction
               -channel
               (createMessageComponentCollector #js
                                                {:componentType
                                                 (.-Button discord/ComponentType)})))
    (let [collector (get-in @state [:collectors channel-id])]
                          (.on collector "collect" handle-collector-event!))))

(defn get-users-in-voice [interaction]
  (convert-users-to-string (.from js/Array
                                  (.. interaction -member -voice -channel -members values))))

(defn interact! [interaction]
  (let [option (or (.. interaction -options (getString "mapmode")) "main")
        server-id (.. interaction -guild -id)
        server-name (.. interaction -guild -name)]
    (println "/go INTERACTION" (js/Date.))
    (go (try
          (let [client (<p! (.connect db/pool))
              server-with-maps (.-rows (<p! (map-server/check-server-with-maps-exists server-id)))]
            (try
              (<p! (db/begin-transaction client))
              (<p! (server/insert-server-if-not-exists client server-id server-name))
              (when (empty? server-with-maps)
                (<p! (map-server/insert-default-maps client server-id)))
              (<p! (db/commit-transaction client))
              (catch js/Error e (do (println e)
                                    (<p! (db/rollback-transaction client))))
              (finally (do (.release client))))
            (let [maps
                  (format-maps (js->clj (.-rows (<p! (map-server/select-maps server-id option)))))]
              (if (.. interaction -member -voice -channel)
                (do
                  (init-interaction interaction maps)
                  (<p! (.reply interaction (create-reply (.-id interaction))))
                  (let [users-in-voice (get-users-in-voice interaction)
                        channel-id (.. interaction -channel -id)]
                    #_(<p! (.followUp interaction users-in-voice))
                    (init-collector interaction channel-id)))
                  (<p! (.reply
                         interaction #js {:content (str "You're not in the voice channel, "
                                                        (.. interaction -member -user -username))
                                          :ephemeral true})))))
          (catch js/Error e (println e))))))



(sort (fn [a b] (- (:t a) (:t b))) [{:t 3}{:t 7}{:t 4}{:t 1} {:t 2}{:t 2}]) ; Thanks! :)
