(ns commands.top
  (:require ["discord.js" :as discord]
            [db.models.player-server-points :as player-server-points]
            [cljs.core.async :refer [go]]
            [cljs.core.async.interop :refer-macros [<p!]]
            [commands.shared.db-utils :as db-utils]
            [commands.shared.player-info :as player-info]))

(def builder
  (.. (discord/SlashCommandBuilder.)
      (setName "top")
      (setDescription "Show top 10")
      toJSON))

(defn in-code-string [value]
  (str "`" value "`"))

(defn interact [interaction]
  (go (try
        (let [server-id (.. interaction -guild -id)
              players (db-utils/get-formatted-rows
                       (<p! (player-server-points/select-top-10 server-id)))
              description (atom "")]
          (doseq [player players]
            (let [{:keys [rank-name wins losses draws total win-rate]}
                  (<p! (player-info/get-details (player "player_id") server-id
                                                      (player "points")))]
              (swap! description str (discord/bold (player "dense_rank")) ". "
                     (discord/bold (player "player"))
                     "\n    Points: " (in-code-string (.toFixed (player "points") 2))
                     "   Rank: " (in-code-string rank-name)
                     "   W: " (in-code-string wins)
                     "   L: " (in-code-string losses)
                     "   D: " (in-code-string draws)
                     "   T: " (in-code-string total)
                     "   WR: " (in-code-string win-rate)
                     "\n")))
          (<p! (.reply interaction #js {:content @description})))
        (catch js/Error e (println "ERROR interact top" e)))))

