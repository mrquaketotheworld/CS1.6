(ns commands.get
  (:require ["discord.js" :as discord]
            ["fs/promises" :as fs]
            [cljs.core.async :refer [go]]
            [cljs.core.async.interop :refer-macros [<p!]]
            [canvas :as canvas-lib]
            [db.models.player :as player]
            [db.models.player-server-points :as player-server-points]
            [db.models.rank :as rank]
            [db.models.player-team-server :as player-team-server]
            [db.models.match :as match]
            [shared.db-utils :as db-utils]
            [shared.constants :refer
              [DARK-GREY GREY TOXIC GREEN ORANGE BLUE LIGHT-BLUE CYAN YELLOW PURPLE RED]]))

(def builder
  (.. (discord/SlashCommandBuilder.)
      (setName "get")
      (setDescription "Get user stats")
      (addUserOption (fn [option]
                         (.. option
                             (setName "user")
                             (setDescription "Select user"))))
      toJSON))

(canvas-lib/registerFont "src/assets/Oswald-Regular.ttf" #js {:family "Oswald Regular"})
(canvas-lib/registerFont "src/assets/Military Poster.ttf" #js {:family "Military Poster Regular"})

(def canvas (canvas-lib/createCanvas 688 276))
(def context (.getContext canvas "2d"))
(def rank-colors {
  "Bot" DARK-GREY
  "Noob" GREY
  "Camper" TOXIC
  "Lucker" GREEN
  "Strawberry Legend" ORANGE
  "Drunken Master" BLUE
  "Rambo" LIGHT-BLUE
  "Terminator" CYAN
  "Legend" YELLOW
  "Professional" PURPLE
  "Nanaxer" RED})

(defn fill-style [color]
 (set! (.-fillStyle context) color))

(defn fill-text [text x y]
 (.fillText context text x y))

(defn global-alpha [number]
 (set! (.-globalAlpha context) number))

(defn font [font-name]
 (set! (.-font context) font-name))

(defn make-context-first-column []
  (fill-style "white"))

(defn make-context-second-column []
  (fill-style (rank-colors "Nanaxer")))

(defn on-first-image-load [interaction]
  (fn [image]
    (go (try
      (let [server-id (.. interaction -guild -id)
            user (or (.. interaction -options (getUser "user")) (.. interaction -user))
            user-id (.-id user)
            username (.-username user)
            player-server (db-utils/get-first-formatted-row
                                (<p! (player-server-points/select-player-by-server
                                      user-id server-id)))]
        (if player-server
          (let [player-info (db-utils/get-first-formatted-row (<p! (player/select-player user-id)))
                player-points (player-server "points")
                rank-name ((db-utils/get-first-formatted-row (<p! (rank/select-rank-by-points
                                                              (.floor js/Math player-points)))) "rank")
                rank-color (rank-colors rank-name)
                team-ids-bulk (db-utils/get-formatted-rows
                           (<p! (player-team-server/select-team-ids user-id server-id)))
                team-ids (clj->js (map #(% "team_id") team-ids-bulk))
                player-rating ((db-utils/get-first-formatted-row
                   (<p! (player-server-points/select-player-rating user-id server-id))) "dense_rank")
                player-team1-wins ((db-utils/get-first-formatted-row
                                     (<p! (match/select-team1-wins team-ids))) "count")
                player-team1-losses ((db-utils/get-first-formatted-row
                                       (<p! (match/select-team1-losses team-ids))) "count")
                player-team1-draws ((db-utils/get-first-formatted-row
                                      (<p! (match/select-team1-draws team-ids))) "count")

                player-team2-wins ((db-utils/get-first-formatted-row
                                     (<p! (match/select-team2-wins team-ids))) "count")
                player-team2-losses ((db-utils/get-first-formatted-row
                                       (<p! (match/select-team2-losses team-ids))) "count")
                player-team2-draws ((db-utils/get-first-formatted-row
                                      (<p! (match/select-team2-draws team-ids))) "count")
                ]
            (println 'W-L-D player-team1-wins player-team1-losses player-team1-draws)
            (println 'W-L-D player-team2-wins player-team2-losses player-team2-draws)
            (fill-style "black")
            (.fillRect context 0 0 (.-width canvas) (.-height canvas))
            (global-alpha 0.22)
            (.drawImage context image 50 0 (.-naturalWidth image) (.-naturalHeight image))
            (global-alpha 1)
            (font "28px Oswald")
            (fill-style "white")

            (make-context-first-column)
            (fill-text "Country" 230 56)
            (make-context-second-column)
            (fill-text (player-info "country") 326 56)

            (make-context-first-column)
            (fill-text "Tag" 230 91)
            (make-context-second-column)
            (fill-text (player-info "tag") 326 91)

            (make-context-first-column)
            (fill-text "Wins" 488 56)
            (make-context-second-column)
            (fill-text "3242" 570 56) ; TODO DB

            (make-context-first-column)
            (fill-text "Losses" 488 91)
            (make-context-second-column)
            (fill-text "3242" 570 91) ; TODO DB

            (make-context-first-column)
            (fill-text "Draws" 488 126)
            (make-context-second-column)
            (fill-text "3232" 570 126) ; TODO DB

            (make-context-first-column)
            (fill-text "Total" 488 161) ; TODO DB
            (make-context-second-column)
            (fill-text "3234" 570 161) ; TODO DB

            (make-context-first-column)
            (fill-text "Win Rate" 488 209)
            (make-context-second-column)
            (fill-text "53%" 595 209) ; TODO DB

            (make-context-first-column)
            (fill-text "Points" 230 209)
            (make-context-second-column)
            (fill-text (.toFixed player-points 2) 326 209)
            (fill-style rank-color)
            (fill-text (str "\"" rank-name"\"") 230 244)

            (make-context-first-column)
            (fill-text "NANAX Points" 488 244)
            (make-context-second-column)
            (fill-text (player-info "nanax_points") 643 244)
            (fill-style "white")
            (font "43px \"Oswald\"")
            (fill-text (str "#" player-rating) 32 244)
            (let [image (<p! (canvas-lib/loadImage
                              (.. interaction -user (displayAvatarURL #js {:extension "jpg"}))))]
              (.drawImage context image 32 32 128 128)
              (font "57px \"Military Poster\"")
              (fill-style rank-color)
              (fill-text username 64 174)
              (<p! (.writeFile fs "src/assets/stats.png" (.toBuffer canvas "image/png")))
              (<p! (.reply interaction #js {:files #js ["src/assets/stats.png"]}))))
          (<p! (.reply interaction
                       #js {:content (str "Sorry, you need to play at "
                                          "least one NANAX match to get your stats, " username)
                            :ephemeral true}))))
      (catch js/Error e (do (println "ERROR on-first-image-load get" e)))))))


(defn interact! [interaction]
  (.then (canvas/loadImage "src/assets/nanax_logo.png") (on-first-image-load interaction)))
