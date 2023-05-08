(ns commands.get ; TODO drop column color, add camper, move right column left -1px
  (:require [discord.js :as discord] ; TODO refactor names
            ["fs/promises" :as fs]
            [cljs.core.async :refer [go]]
            [cljs.core.async.interop :refer-macros [<p!]]
            [canvas :as canvas-lib]
            [db.models.player :as player]
            [db.models.player-server-points :as player-server-points]
            [db.models.rank :as rank]
            [db.models.player-team-server :as player-team-server]
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
            username (.-username user) ; TODO refactor first js->clj
            player-info (db-utils/get-first-formatted-row (<p! (player/select-player user-id)))
            player-points ((db-utils/get-first-formatted-row
                            (<p! (player-server-points/select-player-by-server
                                  user-id server-id))) "points")
            rank-name ((db-utils/get-first-formatted-row (<p! (rank/select-rank-by-points
                                                          (.floor js/Math player-points)))) "rank")
            rank-color (rank-colors rank-name)
            team-ids (db-utils/get-formatted-rows
                       (<p! (player-team-server/select-team-ids user-id server-id)))]
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
        (fill-text "Wins" 489 56)
        (make-context-second-column)
        (fill-text "3242" 571 56) ; TODO DB

        (make-context-first-column)
        (fill-text "Losses" 489 91)
        (make-context-second-column)
        (fill-text "3242" 571 91) ; TODO DB

        (make-context-first-column)
        (fill-text "Draws" 489 126)
        (make-context-second-column)
        (fill-text "3232" 571 126) ; TODO DB

        (make-context-first-column)
        (fill-text "Total" 489 161) ; TODO DB
        (make-context-second-column)
        (fill-text "3234" 571 161) ; TODO DB

        (make-context-first-column)
        (fill-text "Win Rate" 489 209)
        (make-context-second-column)
        (fill-text "53%" 596 209) ; TODO DB

        (make-context-first-column)
        (fill-text "Points" 230 209)
        (make-context-second-column)
        (fill-text player-points 326 209)
        (fill-style rank-color)
        (fill-text (str "\"" rank-name"\"") 230 244)

        (make-context-first-column)
        (fill-text "NANAX Points" 489 244)
        (make-context-second-column)
        (fill-text (player-info "nanax_points") 644 244)
        (fill-style "white")
        (font "43px \"Oswald\"")
        (fill-text "#153" 32 244) ; TODO DB
        (let [image (<p! (canvas-lib/loadImage
                          (.. interaction -user (displayAvatarURL #js {:extension "jpg"}))))]
          (.drawImage context image 32 32 128 128)
          (font "57px \"Military Poster\"")
          (fill-style rank-color)
          (fill-text username 64 174)
          (<p! (.writeFile fs "src/assets/stats.png" (.toBuffer canvas "image/png")))
          (<p! (.reply interaction #js {:files #js ["src/assets/stats.png"]}))))
      (catch js/Error e (do (println "ERROR on-first-image-load get" e)))))))


(defn interact! [interaction]
  (.then (canvas/loadImage "src/assets/nanax_logo.png") (on-first-image-load interaction)))
