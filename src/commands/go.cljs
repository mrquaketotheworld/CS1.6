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
            (println option)

            (let [maps (.-rows (<p! (map-server/select-maps server-id option)))
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
                                                :maps (js->clj maps)
                                                :voted-users {}
                                                :callee-voice-channel-id (.. interaction
                                                                             -member
                                                                             -voice
                                                                             -channel
                                                                             -id)}}}))))
                  
                  ) ; if user in voice, do
                (<p! (.reply
                       interaction #js {:content (str "You're not in the voice channel, "
                                                      (.. interaction -member -user -username))
                                        :ephemeral true})))

              (println @state)


























              )
            (<p! (.reply interaction #js {:content "GO COMMAND"})))
          (catch js/Error e (println e))))))
