(ns commands.go
  (:require ["discord.js" :as discord]
            [cljs.core.async :refer [go]]
            [cljs.core.async.interop :refer-macros [<p!]]))

(def builder
  (.. (discord/SlashCommandBuilder.)
      (setName "go")
      (setDescription "Run map poll!")
      (addStringOption (fn [^js/Object option]
                         (.. option
                             (setName "mapmode")
                             (setDescription "Choose map mode")
                             (addChoices #js {:name "extra" :value "extra"}
                                         #js{:name "fun" :value "fun"}))))
      toJSON))

(defn interact! [^js/Object interaction]
  (go (try (<p! (.reply interaction #js {:content "GO COMMAND"}))(catch js/Error e (println e)))))
