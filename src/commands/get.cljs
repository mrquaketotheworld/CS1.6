(ns commands.get
  (:require [discord.js :as discord] ; TODO refactor names
            ["fs/promises" :as fs]
            [cljs.core.async :refer [go]]
            [cljs.core.async.interop :refer-macros [<p!]]
            [canvas :as canvas-lib]))

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
  "Noob" "#BDBDBD"
  "Lucker" "#00ce21"
  "Strawberry Legend" "#ff8413"
  "Drunken Master" "#13f1ff"
  "Rambo" "#c8ff2c"
  "Terminator" "#009fd1"
  "Terminator 2" "#2c5aff"
  "Professional" "#a513ff"
  "Legend" "#ffd600"
  "Nanaxer" "#d00a0a" })

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
      (fill-style "black")
      (.fillRect context 0 0 (.-width canvas) (.-height canvas))
      (global-alpha 0.22)
      (.drawImage context image 50 0 (.-naturalWidth image) (.-naturalHeight image))
      (global-alpha 1)
      (font "28px Oswald")
      (fill-style "white")

      (make-context-first-column)
      (fill-text "Country" 230 56) ; TODO fix DB
      (make-context-second-column)
      (fill-text "Switzerland" 326 56) ; TODO

      (make-context-first-column)
      (fill-text "Tag" 230 91)
      (make-context-second-column)
      (fill-text "Navi" 326 91) ; TODO

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
      (fill-text "3232" 326 209) ; TODO DB
      (fill-style (rank-colors "Strawberry Legend")) ; TODO DB
      (fill-text "Strawberry Legend" 230 244) ; TODO DB

      (make-context-first-column)
      (fill-text "NANAX Points" 489 244)
      (make-context-second-column)
      (fill-text "5" 644 244)
      (fill-style "white")
      (font "43px \"Oswald\"")
      (fill-text "#153" 32 244) ; TODO DB
      (let [image (<p! (canvas-lib/loadImage
                        (.. interaction -user (displayAvatarURL #js {:extension "jpg"}))))]
        (.drawImage context image 32 32 128 128)
        (font "57px \"Military Poster\"")
        (fill-style (rank-colors "Strawberry Legend")) ; TODO DB
        (fill-text "macautribes" 64 174) ; TODO DB
        (<p! (.writeFile fs "src/assets/stats.png" (.toBuffer canvas "image/png")))
        (<p! (.reply interaction #js {:files #js ["src/assets/stats.png"]})))
      (catch js/Error e (do (println "ERROR on-first-image-load get" e)))))))


(defn interact! [interaction]
  (.then (canvas/loadImage "src/assets/nanax_logo.png") (on-first-image-load interaction)))
