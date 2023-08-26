(ns commands.make-teams
  (:require ["discord.js" :as discord]
            [cljs.core.async :refer [go]]
            [cljs.core.async.interop :refer-macros [<p!]]
            [clojure.string :as string]))

(defn make-teams [players]
  (let [shuffled-players (shuffle players)
        count-half (/ (count shuffled-players) 2)]
    {:team-1 (take count-half shuffled-players)
     :team-2 (take-last count-half shuffled-players)}))

(def builder
  (.. (discord/SlashCommandBuilder.)
      (setName "make-teams")
      (setDescription "Make random teams from voice channel")
      toJSON))

(defn interact! [interaction]
  (go (try
    (let [username (.. interaction -member -user -displayName)
          voice-channel (.. interaction -member -voice -channel)]
      (if voice-channel
        (let [players (.. (.from js/Array (.-members voice-channel))
                          flat
                          (filter (fn [player] (.-user player)))
                          (map (fn [player] (.. player -user -displayName))))]
          (if (even? (count players))
            (let [teams (make-teams players)]
              (<p! (.reply interaction #js {:content
                                      (str "**Team 1:** "
                                         (string/join ", " (:team-1 teams))
                                         "\n" "**Team 2:** " (string/join ", "
                                                      (:team-2 teams)))})))
            (<p! (.reply interaction #js {:content
                                    (str "The number of players in "
                                       "the voice channel must be even, " username)
                                    :ephemeral true}))))
         (<p! (.reply interaction #js {:content (str "You need to be in a voice "
                                                "channel to make teams, " username)
                                            :ephemeral true}))))
  (catch js/Error e (println "ERROR make_teams" e)))))
