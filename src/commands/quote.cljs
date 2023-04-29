(ns commands.quote
  (:require [cljs.core.async :refer [go]]
            [cljs.core.async.interop :refer-macros [<p!]]
            [db.quote :as quote]))


(defn interact! [^js/Object interaction]
  (go (try
        (let [result (<p! (quote/get-quotes-query))
              quotes (js->clj (.-rows result))
              quote-item (get quotes (rand-int (count quotes)))]
          (<p! (.reply interaction #js {
                                        :content (str "> "
                                                      (quote-item "quote")
                                                      " \n "
                                                      (quote-item "author"))
                                        :components #js []})))
        (catch js/Error e (println e)))))

