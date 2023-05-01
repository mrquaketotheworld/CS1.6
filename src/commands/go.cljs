(ns commands.go
  (:require ["discord.js" :as discord]
            [cljs.core.async :refer [go]]
            [cljs.core.async.interop :refer-macros [<p!]]
            [db.connection :as db]
            [db.models.server :as server]
            [db.models.map-server :as map-server]))

(def builder
  (.. (discord/SlashCommandBuilder.)
      (setName "go")
      (setDescription "Run map poll!")
      (addStringOption (fn [^js/Object option]
                         (.. option
                             (setName "mapmode")
                             (setDescription "Choose map mode")
                             (addChoices #js {:name "extra" :value "extra"}
                                         #js {:name "fun" :value "fun"}))))
      toJSON))

(defn create-row [components]
  (println 'components (clj->js components))
  (.addComponents (discord/ActionRowBuilder.) (clj->js components)))

(defn create-button [map-name label is-disabled]
  (println "CREATE" map-name label is-disabled)
    (.. (discord/ButtonBuilder.)
        (setCustomId map-name)
        (setLabel label)
        (setStyle (.-Primary discord/ButtonStyle))
        (setDisabled is-disabled)))

(defn create-buttons [maps]
  (reduce (fn [acc map-group]
            (conj acc (create-row (into [] (map (fn [map-item]
                                         (println map-item)
                                         (create-button
                                           (:map-name map-item)
                                           (:map-name map-item)
                                           (:is-disabled map-item))) map-group)))))
          [] (partition-all 4 maps)))

(defn format-maps [maps]
  (map (fn [map-item]
         {:map-name (map-item "map")
          :voted-users []
          :is-disabled false}) maps))

(def state (atom {}))

(defn interact! [^js/Object interaction]
  (let [option (or (.. interaction -options (getString "mapmode")) "main")
        server-id (.. interaction -guild -id)
        server-name (.. interaction -guild -name)]
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
                  (<p! (.reply interaction #js {:content "GO COMMAND"
                                                :components (clj->js (create-buttons maps))}))
                  
                  
                  ) ; if user in voice, do
                  (<p! (.reply
                         interaction #js {:content (str "You're not in the voice channel, "
                                                        (.. interaction -member -user -username))
                                          :ephemeral true})))))
          (catch js/Error e (println e))))))
