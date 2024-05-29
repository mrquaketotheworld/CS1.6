(ns commands.get
  (:require ["discord.js" :as discord]
            ["axios" :as axios]
            ["sharp" :as sharp]
            [cljs.core.async :refer [go]]
            [cljs.core.async.interop :refer-macros [<p!]]
            [canvas :as canvas-lib]
            [db.models.player :as player]
            [db.models.player-server-points :as player-server-points]
            [commands.shared.db-utils :as db-utils]
            [commands.shared.player-info :as player-info]
            [commands.shared.constants :refer
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
                  "Impressive" RED})

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
  (fill-style (rank-colors "Impressive")))

(defn column-font-normal []
  (font "28px Oswald"))

(defn format-team [team]
  (let [team-width (.. context (measureText team) -width)]
    (when (> team-width 160) (font "18px Oswald"))))

(defn use-set [team]
  (if (= team "UNKNOWN") "use /set" team))

(defn render-column
  [{:keys [first-col-text second-col-text first-col-coords second-col-coords]}]
  (column-font-normal)
  (make-context-first-column)
  (fill-text first-col-text (first first-col-coords) (second first-col-coords))
  (make-context-second-column)
  (fill-text second-col-text (first second-col-coords) (second second-col-coords)))

(defn on-first-image-load [interaction]
  (fn [image]
    (go (try
          (let [server (.-guild interaction)
                server-id (.-id server)
                server-name (.-name server)
                user (or (.. interaction -options (getUser "user")) (.. interaction -user))
                user-id (.-id user)
                username (.-displayName user)
                player-server (db-utils/get-first-formatted-row
                               (<p! (player-server-points/select-player-by-server
                                     user-id server-id)))]
            (if player-server
              (let [team ((db-utils/get-first-formatted-row (<p! (player/select-player user-id))) "tag")
                    player-points (player-server "points")
                    player-rating ((db-utils/get-first-formatted-row
                                    (<p! (player-server-points/select-player-rating
                                          user-id server-id))) "dense_rank")
                    {:keys [rank-name wins losses draws total win-rate round-diff maps-stats]}
                    (<p! (player-info/get-details user-id server-id player-points))
                    rank-color (rank-colors rank-name)
                    formatted-maps-stats (reduce (fn [acc map-stats]
                                                   (let [wins (:wins map-stats)
                                                         losses (:losses map-stats)
                                                         draws (:draws map-stats)
                                                         total (:total map-stats)
                                                         wr (:win-rate map-stats)]
                                                     (str acc (discord/bold (:map map-stats)) "\n"
                                                          "   W: " (player-info/in-code-string wins)
                                                          "   L: " (player-info/in-code-string losses)
                                                          "   D: " (player-info/in-code-string draws)
                                                          "   T: " (player-info/in-code-string total)
                                                          "   WR: " (player-info/in-code-string wr)
                                                          "\n"))) "" maps-stats)
                    bg (<p! (canvas/loadImage (str "src/assets/bg/bg" (rand-int 12) ".jpg")))]

                (.drawImage context bg 0 0 (.-width canvas) (.-height canvas))
                (.drawImage context image 32 212 32 32)
                (set! (.-shadowOffsetX context) 1)
                (set! (.-shadowOffsetY context) 1)
                (set! (.-shadowColor context) "black")
                (set! (.-shadowBlur context) 0)

                (make-context-first-column)
                (font "18px Oswald")
                (fill-text server-name 32 268)
                (column-font-normal)
                (fill-text "Team" 239 56)
                (make-context-second-column)
                (format-team team)
                (fill-text (use-set team) 317 56)

                (render-column {:first-col-text "Points"
                                :second-col-text (.toFixed player-points 2)
                                :first-col-coords [239 91]
                                :second-col-coords [317 91]})

                (render-column {:first-col-text "Round Diff"
                                :second-col-text round-diff
                                :first-col-coords [239 126]
                                :second-col-coords [361 126]})

                (render-column {:first-col-text "Wins"
                                :second-col-text wins
                                :first-col-coords [497 56]
                                :second-col-coords [578 56]})

                (render-column {:first-col-text "Losses"
                                :second-col-text losses
                                :first-col-coords [497 91]
                                :second-col-coords [578 91]})

                (render-column {:first-col-text "Draws"
                                :second-col-text draws
                                :first-col-coords [497 126]
                                :second-col-coords [578 126]})

                (render-column {:first-col-text "Total"
                                :second-col-text total
                                :first-col-coords [497 161]
                                :second-col-coords [578 161]})

                (render-column {:first-col-text "Win Rate"
                                :second-col-text win-rate
                                :first-col-coords [497 244]
                                :second-col-coords [604 244]})

                (fill-style rank-color)
                (fill-text (str "\"" rank-name "\"") 239 244)

                (fill-style "white")
                (font "43px \"Oswald\"")
                (fill-text (str "#" player-rating) 72 244)
                (let [image (<p! (canvas-lib/loadImage
                                  (.displayAvatarURL user #js {:extension "jpg"})))]
                  (set! (.-shadowOffsetX context) 0)
                  (set! (.-shadowOffsetY context) 0)
                  (.drawImage context image 32 32 128 128)
                  (set! (.-shadowOffsetX context) 3)
                  (set! (.-shadowOffsetY context) 3)
                  (font "58px \"Military Poster\"")
                  (fill-style rank-color)
                  (fill-text username 104 180)
                  (<p! (.reply interaction #js {:files #js [(discord/AttachmentBuilder.
                                                             (.toBuffer canvas "image/png"))]}))
                  (<p! (.followUp interaction #js {:content formatted-maps-stats}))))
              (<p! (.reply interaction
                           #js {:content (str "Sorry, you need to play at "
                                              "least one **" server-name "** match to get your stats, "
                                              username)
                                :ephemeral true}))))
          (catch js/Error e (println "ERROR on-first-image-load get" e))))))

(defn interact! [interaction]
  (go (try
        (let [image-response (<p! (.get axios (.. interaction -guild (iconURL)) #js {:responseType "arraybuffer"}))
              image (<p! (.. (sharp (.-data image-response)) (toFormat "png") (toBuffer)))]
          (.then (canvas/loadImage image) (on-first-image-load interaction)))

        (catch js/Error e (println "ERROR interact!" e)))))


