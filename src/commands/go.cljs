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

(defn create-row [components]
  (.addComponents (discord/ActionRowBuilder.) (clj->js components)))

(defn create-button [map-name label is-disabled]
  (.. (discord/ButtonBuilder.)
      (setCustomId map-name)
      (setLabel label)
      (setStyle (.-Primary discord/ButtonStyle))
      (setDisabled is-disabled)))

(defn create-buttons [maps]
  (clj->js (reduce (fn [acc map-group]
             (conj acc (create-row (into [] (map (fn [map-item]
                                                   (create-button
                                                     (:map-name map-item)
                                                     (:map-name map-item)
                                                     (:is-disabled map-item))) map-group)))))
           [] (partition-all 4 maps))))

(defn format-maps [maps]
  (map (fn [map-item]
         {:map-name (map-item "map")
          :voted-users []
          :is-disabled false}) maps))

(defn create-reply [title maps]
  #js {:content title
       :components (create-buttons maps)})

(defn convert-users-to-string [users]
  (reduce (fn [acc user]
            (str acc "<@" (.. user -user -id) ">")) "" users))

(defn handle-collector-event [event]
  (println event))

(def MAP-QUESTION ":triangular_flag_on_post: **VOTE FOR THE MAP PLEASE**, **1 MINUTE TO VOTE**")

(def state (atom {}))

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
              (finally (do (.release client)
                           (println "RELEASE CLIENT")))) ; remove TESTING stuff

            (let [maps
                  (format-maps (js->clj (.-rows (<p! (map-server/select-maps server-id option)))))
                  guild-id (.. interaction -guild -id)
                  interaction-id (.-id interaction)]
              (if (.. interaction -member -voice -channel)
                (do
                  (when-not (contains? @state guild-id)
                    (swap! state
                           (fn [old-state]
                                 (assoc old-state
                                        guild-id
                                        {:collectors {}
                                         :interactions {
                                                interaction-id {
                                                :maps maps
                                                :callee-voice-channel-id (.. interaction
                                                                             -member
                                                                             -voice
                                                                             -channel
                                                                             -id)}}}))))
                  (<p! (.reply interaction (create-reply MAP-QUESTION maps)))
                  (let [users-in-voice
                        (convert-users-to-string (.from js/Array
                                (.. interaction -member -voice -channel -members values)))
                        channel-id (.. interaction -channel -id)]
                    (<p! (.followUp interaction users-in-voice))
                    (when-not (get-in @state [guild-id :collectors channel-id])
                      (swap! state assoc-in [guild-id :collectors channel-id]
                             (.. interaction
                                 -channel
                                 (createMessageComponentCollector #js
                                                              {:componentType
                                                             (.-Button discord/ComponentType)}))))
                    (let [collector (get-in @state [guild-id :collectors channel-id])]
                      (.on collector "collect" handle-collector-event)))) ; if user in voice, do
                  (<p! (.reply
                         interaction #js {:content (str "You're not in the voice channel, "
                                                        (.. interaction -member -user -username))
                                          :ephemeral true})))))
          (catch js/Error e (println e))))))




(def x (atom {:cars {:a "Toyota" :b "BMW"}}))

(swap! x assoc-in [:cars :a] "Suzuki")
