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

(defn interact! [^js/Object interaction]
  (let [option (.. interaction -options (getString "mapmode"))
        server-id (.. interaction -guild -id)
        server-name (.. interaction -guild -name)]
    (go (try
        (let [client (<p! (.connect db/pool))
              server-with-maps (<p! (map-server/check-server-with-maps-exists server-id))]
          (try
            (<p! (db/begin-transaction client))
            (<p! (server/insert-server-if-not-exists client server-id server-name))
            (when (empty? (.-rows server-with-maps))
              (<p! (map-server/insert-default-maps client server-id)))
            (<p! (db/commit-transaction client))
            (catch js/Error e (do (println e)
                                (<p! (db/rollback-transaction client))))
            (finally (do (.release client)
                         (println "RELEASE CLIENT")))) ; remove TESTING stuff

            ; (case option
            ;   "extra" (println "extra")
            ;   "fun" (println "fun")
            ;   (println "default"))
            (<p! (.reply interaction #js {:content "GO COMMAND"})))
          (catch js/Error e (println e))))))
