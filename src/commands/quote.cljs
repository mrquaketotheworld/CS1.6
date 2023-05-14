(ns commands.quote
  (:require ["discord.js" :as discord]
            [cljs.core.async :refer [go]]
            [cljs.core.async.interop :refer-macros [<p!]]
            [db.models.quote :as quote]))

(def builder
  (.. (discord/SlashCommandBuilder.)
      (setName "quote")
      (setDescription "Show random player's quote")
      (addStringOption (fn [option]
                         (.. option
                             (setName "word")
                             (setDescription "Search quote or author"))))
      toJSON))

(defn interact! [interaction]
  (let [word (.. interaction -options (getString "word"))]
    (go (try
          (<p! (.reply interaction
                       (if word
                         (let [result (<p! (quote/search-word-quote word))
                               quotes (js->clj (.-rows result))
                               quote-item (get quotes (rand-int (count quotes)))]
                           (if quote-item
                             #js {:content (str "> "
                                                (quote-item "quote")
                                                " \n "
                                                (quote-item "author"))}
                             #js {:content (str "Sorry, word **" word "** not found.")
                                  :ephemeral true}))
                         (let [result (<p! (quote/get-quotes-query))
                               quotes (js->clj (.-rows result))
                               quote-item (get quotes (rand-int (count quotes)))]
                           #js {:content (str "> "
                                              (quote-item "quote")
                                              " \n "
                                              (quote-item "author"))}))))

          (catch js/Error e (println "ERROR interact! quote" e))))))

