(ns commands.shared.db-utils)

(defn get-formatted-rows [query-result]
  (js->clj (.-rows query-result)))

(defn get-first-formatted-row [query-result]
  (first (get-formatted-rows query-result)))
