(ns commands.make-random-teams
  (:require [cljs.core.async :refer [go]]
            [cljs.core.async.interop :refer-macros [<p!]]))

(defn interact! [^js/Object interaction]
  (go (try (<p! (.reply interaction #js {:content "make-random-teams"
                                         :components #js []}))
           (catch js/Error e (js/console.log "ERROR /quote" e)))))
