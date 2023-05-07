(ns commands.get
  (:require [discord.js :as discord] ; TODO refactor names
            [fs :as fs]
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
  (fill-style (rank-colors "Nanaxer"))) ; TODO fix

(defn make-context-second-column []
  (fill-style "white"))

(defn on-first-image-load [image]
  (.log js/console image)
  (fill-style "black")
  (.fillRect context 0 0 (.-width canvas) (.-height canvas))
  (global-alpha 0.22)
  (.drawImage context image 50 0 (.-naturalWidth image) (.-naturalHeight image))
  (global-alpha 1)
  (font "28px Oswald")
  (fill-style "white")

  (make-context-first-column)
  (fill-text "Country" 230 55) ; TODO fix DB
  (make-context-second-column)
  (fill-text "Switzerland" 326 55) ; TODO

  (make-context-first-column)
  (fill-text "Tag" 230 90)
  (make-context-second-column)
  (fill-text "Navi" 326 90)

  (make-context-first-column)
  (fill-text "Wins" 488 55)
  (make-context-second-column)
  (fill-text "3242" 570 55) ; TODO DB

  (make-context-first-column)
  (fill-text "Losses" 488 90)
  (make-context-second-column)
  (fill-text "3242" 570 90) ; TODO DB

  (make-context-first-column)
  (fill-text "Draws" 488 125)
  (make-context-second-column)
  (fill-text "3232" 570 125) ; TODO DB

  (make-context-first-column)
  (fill-text "Total" 488 160) ; TODO DB
  (make-context-second-column)
  (fill-text "3234" 570 160) ; TODO DB

  (make-context-first-column)
  (fill-text "Win Rate" 488 210)
  (make-context-second-column)
  (fill-text "53%" 595 210) ; TODO DB

  (make-context-first-column)
  (fill-text "Points" 230 210)
  (make-context-second-column)
  (fill-text "3232" 326 210) ; TODO DB
  (fill-style (rank-colors "Strawberry Legend")) ; TODO DB
  (fill-text "Strawberry Legend" 230 245) ; TODO DB

  (make-context-first-column)
  (fill-text "NANAX Points" 488 245)
  (make-context-second-column)
  (fill-text "5" 643 245)
  (.stroke context)




  (.writeFile fs "src/assets/stats.png" (.toBuffer canvas "image/png") (fn [err]
                   (if err
                     (println "ERROR writeFile get" err)
                     (println "SUCCESS"))))
  )

  (.then (canvas/loadImage "src/assets/nanax_logo.png") on-first-image-load)

(defn interact! [interaction]
  )
