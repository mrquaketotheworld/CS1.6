(ns commands.shared.player-info
  (:require [cljs.core.async :refer [go]]
            [cljs.core.async.interop :refer-macros [<p!]]
            [db.models.player-team-server :as player-team-server]
            [db.models.match :as match]
            [commands.shared.db-utils :as db-utils]))

(defn get-matches-stats [user-id server-id]
  (js/Promise. (fn [resolve]
                 (go
                   (let [team-ids-bulk (db-utils/get-formatted-rows
                                      (<p! (player-team-server/select-team-ids user-id server-id)))
                         team-ids (clj->js (map #(% "team_id") team-ids-bulk))
                         player-team1-wins (js/Number ((db-utils/get-first-formatted-row
                                                (<p! (match/select-team1-wins team-ids))) "count"))
                         player-team1-losses (js/Number ((db-utils/get-first-formatted-row
                                              (<p! (match/select-team1-losses team-ids))) "count"))
                         player-team1-draws (js/Number ((db-utils/get-first-formatted-row
                                               (<p! (match/select-team1-draws team-ids))) "count"))

                         player-team2-wins (js/Number ((db-utils/get-first-formatted-row
                                                (<p! (match/select-team2-wins team-ids))) "count"))
                         player-team2-losses (js/Number ((db-utils/get-first-formatted-row
                                              (<p! (match/select-team2-losses team-ids))) "count"))
                         player-team2-draws (js/Number ((db-utils/get-first-formatted-row
                                               (<p! (match/select-team2-draws team-ids))) "count"))
                         player-total-wins (+ player-team1-wins player-team2-wins)
                         player-total-losses (+ player-team1-losses player-team2-losses)
                         player-total-draws (+ player-team1-draws player-team2-draws)
                         player-total-matches
                           (+ player-total-wins player-total-losses player-total-draws)
                         player-win-rate (* (/ 100 player-total-matches) player-total-wins)]
                     (resolve {:wins player-total-wins
                               :losses player-total-losses
                               :draws player-total-draws
                               :total player-total-matches
                               :win-rate player-win-rate}))))))
