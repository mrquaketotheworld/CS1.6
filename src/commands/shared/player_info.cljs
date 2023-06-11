(ns commands.shared.player-info
  (:require [cljs.core.async :refer [go]]
            [cljs.core.async.interop :refer-macros [<p!]]
            [db.models.player-team-server :as player-team-server]
            [db.models.match :as match]
            [db.models.rank :as rank]
            [commands.shared.db-utils :as db-utils]))

(defn get-number-count [matches]
  (js/Number (count matches)))

(defn get-diff-round-team1 [matches]
  (reduce (fn [acc match] (+ acc (- (match "team1_score") (match "team2_score")))) 0 matches))

(defn get-diff-round-team2 [matches]
  (reduce (fn [acc match] (+ acc (- (match "team2_score") (match "team1_score")))) 0 matches))

(defn get-maps [team1_matches team2_matches]
  (let [matches (concat team1_matches team2_matches)]
    (reduce (fn [acc match] (let [map-name (match "map")]
                              (assoc acc map-name (inc (acc map-name))))) {} matches)))

(defn win-rate [matches wins]
  (let [wr (* (/ 100 matches) wins)]
    (str (.toFixed
          (if (js/isNaN wr) 0 wr)) "%")))

(defn get-maps-all-stats [wins losses draws]
  (let [maps (set (concat (keys wins) (keys losses) (keys draws)))]
    (sort-by :total > (reduce (fn [acc map-name] (let [wins-map (int (wins map-name))
                                                       losses-map (int (losses map-name))
                                                       draws-map (int (draws map-name))
                                                       total-map (+ wins-map losses-map draws-map)]
                                                   (conj acc {:map map-name
                                                              :wins wins-map
                                                              :losses losses-map
                                                              :draws draws-map
                                                              :total total-map
                                                              :win-rate (win-rate total-map wins-map)})))
                              [] maps))))

(defn in-code-string [value]
  (str "`" value "`"))

(defn get-details [user-id server-id player-points]
  (js/Promise. (fn [resolve]
                 (go
                   (let [rank-name ((db-utils/get-first-formatted-row
                                     (<p! (rank/select-rank-by-points
                                           (.floor js/Math player-points)))) "rank")
                         team-ids-bulk (db-utils/get-formatted-rows
                                        (<p! (player-team-server/select-team-ids user-id server-id)))
                         team-ids (clj->js (map #(% "team_id") team-ids-bulk))
                         player-team1-wins (db-utils/get-formatted-rows
                                            (<p! (match/select-team1-wins team-ids)))
                         player-team1-losses (db-utils/get-formatted-rows
                                              (<p! (match/select-team1-losses team-ids)))
                         player-team1-draws (db-utils/get-formatted-rows
                                             (<p! (match/select-team1-draws team-ids)))

                         player-team2-wins (db-utils/get-formatted-rows
                                            (<p! (match/select-team2-wins team-ids)))
                         player-team2-losses (db-utils/get-formatted-rows
                                              (<p! (match/select-team2-losses team-ids)))
                         player-team2-draws (db-utils/get-formatted-rows
                                             (<p! (match/select-team2-draws team-ids)))
                         player-total-wins (+ (get-number-count player-team1-wins)
                                              (get-number-count player-team2-wins))
                         player-total-losses (+ (get-number-count player-team1-losses)
                                                (get-number-count player-team2-losses))
                         player-total-draws (+ (get-number-count player-team1-draws)
                                               (get-number-count player-team2-draws))
                         player-total-matches (+ player-total-wins player-total-losses player-total-draws)
                         player-win-rate (win-rate player-total-matches player-total-wins)]
                     (resolve {:rank-name rank-name
                               :wins player-total-wins
                               :losses player-total-losses
                               :draws player-total-draws
                               :total player-total-matches
                               :maps-stats (get-maps-all-stats
                                            (get-maps player-team1-wins player-team2-wins)
                                            (get-maps player-team1-losses player-team2-losses)
                                            (get-maps player-team1-draws player-team2-draws))
                               :win-rate player-win-rate
                               :round-diff (+ (get-diff-round-team1 player-team1-wins)
                                              (get-diff-round-team1 player-team1-losses)
                                              (get-diff-round-team2 player-team2-wins)
                                              (get-diff-round-team2 player-team2-losses))}))))))
