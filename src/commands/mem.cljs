(ns commands.mem
  (:require
   ["discord.js" :as discord]
   [canvas :as canvas-lib]
   [cljs.core.async :refer [go]]
   [cljs.core.async.interop :refer-macros [<p!]]))

(def builder
  (.. (discord/SlashCommandBuilder.)
      (setName "mem")
      (setDescription "Create a mem")
      (addSubcommand (fn [subcommand]
                       (.. subcommand
                           (setName "monkey")
                           (setDescription "Monkey arguing")
                           (addStringOption (fn [option]
                                              (.. option
                                                  (setName "phrase")
                                                  (setRequired true)
                                                  (setMaxLength 32)
                                                  (setDescription "Monkey says")))))))
      (addSubcommand (fn [subcommand]
                       (.. subcommand
                           (setName "batman")
                           (setDescription "Batman slaps")
                           (addStringOption (fn [option]
                                              (.. option
                                                  (setName "person-phrase")
                                                  (setRequired true)
                                                  (setMaxLength 24)
                                                  (setDescription "Person says"))))
                           (addStringOption (fn [option]
                                              (.. option
                                                  (setName "batman-phrase")
                                                  (setRequired true)
                                                  (setMaxLength 24)
                                                  (setDescription "Batman says")))))))
      (addSubcommand (fn [subcommand]
                       (.. subcommand
                           (setName "girl")
                           (setDescription "Girl complaints")
                           (addStringOption (fn [option]
                                              (.. option
                                                  (setName "phrase")
                                                  (setRequired true)
                                                  (setMaxLength 32)
                                                  (setDescription "Girl says")))))))
      (addSubcommand (fn [subcommand]
                       (.. subcommand
                           (setName "harold-thumbsup")
                           (setDescription "Pain Harold thumbs up")
                           (addStringOption (fn [option]
                                              (.. option
                                                  (setName "phrase")
                                                  (setRequired true)
                                                  (setMaxLength 32)
                                                  (setDescription "Harold says")))))))

      toJSON))

(canvas-lib/registerFont "src/assets/Oswald-Regular.ttf" #js {:family "Oswald Regular"})
(def monkey (canvas/loadImage "src/assets/monkey.jpg"))
(def batman (canvas/loadImage "src/assets/batman.jpg"))
(def girl (canvas/loadImage "src/assets/girl.jpg"))
(def harold-thumbsup (canvas/loadImage "src/assets/harold-thumbsup.jpeg"))

(defn upper-case [string]
  (.toUpperCase string))

(defn render-image [callback]
  (fn [image]
    (let [canvas (canvas-lib/createCanvas (.-naturalWidth image) (.-naturalHeight image))
          context (.getContext canvas "2d")]
      (.drawImage context image 0 0)
      (set! (.-fillStyle context) "white")
      (set! (.-textAlign context) "center")
      (set! (.-shadowColor context) "black")
      (set! (.-shadowOffsetX context) "1")
      (set! (.-shadowOffsetY context) "1")
      (set! (.-shadowBlur context) "0")
      (callback canvas context)
      canvas)))

(defn render-monkey [options]
  (.then monkey (render-image (fn [canvas context]
                                (set! (.-font context) "38px Oswald")
                                (.fillText context
                                           (upper-case
                                            (.getString options "phrase")) (/ (.-width canvas) 2) 60)))))

(defn render-batman [options]
  (.then batman (render-image (fn [_ context]
                                (let [person-phrase (upper-case (.getString options "person-phrase"))
                                      batman-phrase (upper-case (.getString options "batman-phrase"))]
                                  (set! (.-font context) "27px Oswald")
                                  (set! (.-fillStyle context) "black")
                                  (set! (.-shadowColor context) "white")
                                  (.fillText context person-phrase 172 70)
                                  (.fillText context batman-phrase 530 85))))))

(defn render-girl [options]
  (.then girl (render-image (fn [canvas context]
                              (set! (.-font context) "63px Oswald")
                              (.fillText context
                                         (upper-case
                                          (.getString options "phrase")) (/ (.-width canvas) 2) 90)))))

(defn render-harold-thumbsup [options]
  (.then harold-thumbsup (render-image (fn [canvas context]
                                         (set! (.-font context) "54px Oswald")
                                         (set! (.-fillStyle context) "black")
                                         (set! (.-shadowColor context) "white")
                                         (.fillText context
                                                    (upper-case
                                                     (.getString options "phrase")) (/ (.-width canvas) 2) 90)))))

(defn render-subcommand [subcommand options]
  (case subcommand
    "monkey" (render-monkey options)
    "batman" (render-batman options)
    "girl" (render-girl options)
    "harold-thumbsup" (render-harold-thumbsup options)))

(defn interact! [interaction]
  (println "/mem " (js/Date.))
  (let [options (.-options interaction)
        subcommand (.getSubcommand options)]
    (go
      (try
        (<p! (.reply interaction #js {:files #js [(discord/AttachmentBuilder.
                                                   (.toBuffer (<p! (render-subcommand subcommand options)) "image/png"))]}))
        (catch js/Error e (println "ERROR interact! mem" e))))))
