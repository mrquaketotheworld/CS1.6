(ns commands.quote
  (:require [cljs.core.async :refer [go]]
            [cljs.core.async.interop :refer-macros [<p!]]
            [db.quotes :as quotes]))

(def shuffled-quotes (atom (shuffle quotes/data)))
(def counter (atom 0))

(defn interact! [^js/Object interaction]
  (let [quote-item (get @shuffled-quotes @counter)]
    (if quote-item
      (do (swap! counter inc)
        (go (try (<p! (.reply interaction #js {
                                               :content (str "> "
                                                    (:quote quote-item)
                                                    " \n "
                                                    (:author quote-item))
                                               :components #js []}))
                 (catch js/Error e (js/console.log "ERROR /quote" e)))))
      (do (reset! counter 0)
        (reset! shuffled-quotes (shuffle quotes/data))
        (recur interaction)))))

