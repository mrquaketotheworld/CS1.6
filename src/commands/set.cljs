(ns commands.set
  (:require ["discord.js" :as discord]
            [cljs.core.async :refer [go]]
            [cljs.core.async.interop :refer-macros [<p!]]))

(def builder
  (.. (discord/SlashCommandBuilder.)
      (setName "set")
      (setDescription "Set some fields in your stats card")
      (addStringOption (fn [option]
                         (.. option
                             (setName "tag")
                             (setDescription "Enter your tag, 1-10 characters")
                             (setMinLength 1)
                             (setMaxLength 10))))
      toJSON))


(defn interact! [interaction]
  (go (try
        (let [tag (.. interaction -options (getString "tag"))]
          (if tag
            (do
              
              (<p! (.reply interaction tag)))
            (<p! (.reply interaction #js {:content "You have not changed any settings"
                                          :ephemeral true}))))
        (catch js/Error e (println "ERROR interact! set" e)))))
