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
                                                     (:map-name map-item) ; TODO
                                                     (:is-disabled map-item))) map-group)))))
           [] (partition-all 4 maps))))

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

(defmulti create-reply (fn [reply-type content components] reply-type))
(defmethod create-reply :start [reply-type content maps]
  #js {:content content
       :components (create-buttons maps)})
(defmethod create-reply :vote [reply-type content maps])
(defmethod create-reply :finish [reply-type content components] {:x "test"})

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
      (do (if (= callee-voice-channel-id (.-id voice-channel))
            (println 'GOOD)
            (wrong-vote-reply event callee-voice-channel-id username)))
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
                  (<p! (.reply interaction (create-reply :start MAP-QUESTION maps)))
                  (let [users-in-voice (get-users-in-voice interaction)
                        channel-id (.. interaction -channel -id)]
                    #_(<p! (.followUp interaction users-in-voice))
                    (init-collector interaction channel-id)))
                  (<p! (.reply
                         interaction #js {:content (str "You're not in the voice channel, "
                                                        (.. interaction -member -user -username))
                                          :ephemeral true})))))
          (catch js/Error e (println e))))))
