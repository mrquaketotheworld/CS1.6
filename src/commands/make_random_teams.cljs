(ns commands.make-random-teams
  (:require [cljs.core.async :refer [go]]
            [cljs.core.async.interop :refer-macros [<p!]]
            [clojure.string :as string]))

(def error-message "ERROR /make-random-teams" )

(defn make-random-teams [players]
  (let [shuffled-players (shuffle players)
        count-half (/ (count shuffled-players) 2)]
    {:team-1 (take count-half shuffled-players)
     :team-2 (take-last count-half shuffled-players)}))

(defn interact! [^js/Object interaction]
  (let [username (.. interaction -member -user -username)
        voice-channel (.. interaction -member -voice -channel)]
    (if voice-channel
      (let [players (.. (.from js/Array (.-members voice-channel))
                        flat
                        (filter (fn [^js/Object player] (.-user player)))
                        (map (fn [^js/Object player] (.. player -user -username))))]
        (if (even? (count players))
          (let [teams (make-random-teams players)]
            (go (try (<p! (.reply interaction #js {:content
                                                   (str "**Team 1:** "
                                                   (string/join ", " (:team-1 teams))
                                                   "\n" "**Team 2:** " (string/join ", "
                                                    (:team-2 teams)))}))
                     (catch js/Error e (println error-message e)))))
          (go (try (<p! (.reply interaction #js {:content
                                                 (str "The number of players in "
                                                      "the voice channel must be even, " username)
                                                 :ephemeral true}))
                   (catch js/Error e (println error-message e))))))
      (go (try (<p! (.reply interaction #js {:content (str "You need to be in a voice "
                                                           "channel to make teams, " username)
                                             :ephemeral true}))
                 (catch js/Error e (println error-message e)))))))

