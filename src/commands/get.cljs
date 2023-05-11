(ns commands.get
  (:require ["discord.js" :as discord]
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
(def rank-colors {"Bot" DARK-GREY
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

(defn column-font-normal []
  (font "28px Oswald"))

(defn format-tag [tag]
  (let [tag-width (.. context (measureText tag) -width)]
    (when (> tag-width 160) (font "18px Oswald"))))

(defn on-first-image-load [interaction]
  (fn [image]
    (go (try
      (let [server (.-guild interaction)
            server-id (.-id server)
            server-name (.-name server)
            user (or (.. interaction -options (getUser "user")) (.. interaction -user))
            user-id (.-id user)
            username (.-username user)
            player-server (db-utils/get-first-formatted-row
                                (<p! (player-server-points/select-player-by-server
                                      user-id server-id)))]
        (if player-server
          (let [player-info (db-utils/get-first-formatted-row (<p! (player/select-player user-id)))
                tag (player-info "tag")
                player-points (player-server "points")
                rank-name ((db-utils/get-first-formatted-row (<p! (rank/select-rank-by-points
                                                          (.floor js/Math player-points)))) "rank")
                rank-color (rank-colors rank-name)
                team-ids-bulk (db-utils/get-formatted-rows
                           (<p! (player-team-server/select-team-ids user-id server-id)))
                team-ids (clj->js (map #(% "team_id") team-ids-bulk))
                player-rating ((db-utils/get-first-formatted-row
                 (<p! (player-server-points/select-player-rating user-id server-id))) "dense_rank")
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
                player-total-matches (+ player-total-wins player-total-losses player-total-draws)
                player-win-rate (* (/ 100 (+ player-total-wins player-total-losses))
                                   player-total-wins)]
            (fill-style "black")
            (.fillRect context 0 0 (.-width canvas) (.-height canvas))
            (global-alpha 0.1)
            (.drawImage context image 50 1 (.-naturalWidth image) (.-naturalHeight image))
            (global-alpha 1)
            (column-font-normal)
            (fill-style "white")

            (make-context-first-column)
            (fill-text "Tag" 239 56)
            (make-context-second-column)
            (format-tag tag)
            (fill-text tag 317 56)
            (column-font-normal)

            (make-context-first-column)
            (fill-text "Points" 239 91)
            (make-context-second-column)
            (fill-text (.toFixed player-points 2) 317 91)

            (make-context-first-column)
            (fill-text "Wins" 497 56)
            (make-context-second-column)
            (fill-text player-total-wins 578 56)

            (make-context-first-column)
            (fill-text "Losses" 497 91)
            (make-context-second-column)
            (fill-text player-total-losses 578 91)

            (make-context-first-column)
            (fill-text "Draws" 497 126)
            (make-context-second-column)
            (fill-text player-total-draws 578 126)

            (make-context-first-column)
            (fill-text "Total" 497 161)
            (make-context-second-column)
            (fill-text player-total-matches 578 161)

            (make-context-first-column)
            (fill-text "Win Rate" 497 244)
            (make-context-second-column)
            (fill-text (str (.toFixed (if (js/isNaN player-win-rate) 0 player-win-rate)) "%")
                       604 244)

            (fill-style rank-color)
            (fill-text (str "\"" rank-name"\"") 239 244)

            (fill-style "white")
            (font "43px \"Oswald\"")
            (fill-text (str "#" player-rating) 32 244)
            (let [image (<p! (canvas-lib/loadImage
                              (.displayAvatarURL user #js {:extension "jpg"})))]
              (.drawImage context image 32 32 128 128)
              (font "58px \"Military Poster\"")
              (fill-style rank-color)
              (fill-text username 104 180)
              (<p! (.reply interaction #js {:files #js [(discord/AttachmentBuilder.
                                                          (.toBuffer canvas "image/png"))]}))))
          (<p! (.reply interaction
                       #js {:content (str "Sorry, you need to play at "
                                          "least one **" server-name "** match to get your stats, "
                                          username)
                            :ephemeral true}))))
      (catch js/Error e (println "ERROR on-first-image-load get" e))))))


(defn interact! [interaction]
  (.then (canvas/loadImage "src/assets/nanax_logo.png") (on-first-image-load interaction)))
