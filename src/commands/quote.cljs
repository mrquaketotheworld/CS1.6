(ns commands.quote
  (:require [db.quotes :as quotes]
            [cljs.core.async :refer [go]]
            [cljs.core.async.interop :refer-macros [<p!]]))

(def shuffled-quotes (shuffle quotes/quotes))
(defn quote [^js/Object interaction]
(js/console.log (clj->js (get shuffled-quotes 1)))
  (go (try (<p! (.reply interaction #js {:content "hello from quote" :components #js []}))
           (catch js/Error e (js/console.log "ERROR /quote" e)))))

