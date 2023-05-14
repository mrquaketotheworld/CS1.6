(ns commands.go
  (:require ["discord.js" :as discord]
            [cljs.core.async :refer [go]]
            [cljs.core.async.interop :refer-macros [<p!]]
            [db.models.map-server :as map-server]
            [commands.shared.db-utils :as db-utils]))

(def builder
  (.. (discord/SlashCommandBuilder.)
      (setName "go")
      (setDescription "Run map poll")
      (addStringOption (fn [option]
                         (.. option
                             (setName "mapmode")
                             (setDescription "Choose map mode")
                             (addChoices #js {:name "extra" :value "extra"}
                                         #js {:name "fun" :value "fun"}))))
      toJSON))

(def state (atom {:interactions {}}))

(def ERROR-MESSAGE-INTERACT "ERROR interact! go")

(def emoji-maps {
  "Inferno" ":fire:"
  "Nuke" ":radioactive:"
  "Train" ":station:"
  "Cache" ":dollar:"
  "Tuscan" ":palm_tree:"
  "Dust2" ":duck:"
  "Mirage" ":firecracker:"
})

(def emoji-numbers {
  "1" ":one:"
  "2" ":two:"
  "3" ":three:"
  "4" ":four:"
  "5" ":five:"
  "6" ":six:"
  "7" ":seven:"
  "8" ":eight:"
  "9" ":nine:"
})

(defn format-map-name [map-name]
  (let [emoji-start-end (emoji-maps map-name)]
    (str (reduce (fn [acc char]
                   (str acc (if (js/isNaN char)
                              (str ":regional_indicator_" (.toLowerCase char) ":")
                              (emoji-numbers char))))
                               (str emoji-start-end " ") map-name) " " emoji-start-end)))

(defn get-maps [interaction-id]
  (get-in @state [:interactions interaction-id :maps]))

(defn create-row [components]
  (.addComponents (discord/ActionRowBuilder.) (clj->js components)))

(defn create-button [map-name label is-disabled winner-map]
  (.. (discord/ButtonBuilder.)
      (setCustomId map-name)
      (setLabel label)
      (setStyle (if (= map-name winner-map)
                       (.-Danger discord/ButtonStyle)
                       (.-Primary discord/ButtonStyle)))
      (setDisabled is-disabled)))

(defn create-buttons [interaction-id]
  (let [winner-map (get-in @state [:interactions interaction-id :winner-map])]
   (clj->js (reduce (fn [acc map-group]
             (conj acc (create-row (into [] (map (fn [map-item]
                                                   (let [map-name (:map-name map-item)
                                                         is-disabled (:is-disabled map-item)
                                                         map-label
                                                           (if is-disabled
                                                                 (str map-name " "
                                                                   (count (:voted-users map-item)))
                                                                  map-name)]
                                                     (create-button
                                                       map-name
                                                       map-label
                                                       is-disabled
                                                       winner-map))) map-group)))))
           [] (partition-all 4 (get-maps interaction-id))))))

(defn disable-buttons [interaction-id]
  (swap! state update-in [:interactions interaction-id :maps]
         #(map (fn [map-item] (assoc map-item :is-disabled true)) %)))

(defn get-voted-users [maps]
  (sort (fn [a b] (- (:timestamp a) (:timestamp b)))
        (reduce (fn [acc map-item] (concat acc (:voted-users map-item))) [] maps)))

(defn calculate-winner-map [interaction-id]
  (let [winner-candidates (->> (get-maps interaction-id)
        (map (fn [map-item]
               {:map-name (:map-name map-item)
                :votes (->> map-item
                            :voted-users
                            count)}))
        (sort-by :votes >)
        (partition-by :votes)
        first)
        winner-map (->> winner-candidates rand-nth :map-name)]
    (swap! state assoc-in [:interactions interaction-id :winner-map] winner-map)
    (if (> (count winner-candidates) 1)
      (let [candidates-string (reduce (fn [acc candidate]
                (str acc (discord/bold (:map-name candidate)) "? ")) "" winner-candidates)]
        (->> winner-map
             format-map-name
             (str candidates-string "Random: ")))
      (format-map-name winner-map))))

(defn create-users-list [maps]
  (let [users-list-string
        (reduce (fn [acc voted-user]
                  (str acc ":jigsaw: " (:username voted-user)
                       (when (:is-disabled (first maps)) (str ": " (:map-name voted-user))) "\n"))
                "" (get-voted-users maps))]
    (if (empty? users-list-string) "" users-list-string)))

(defn create-content [interaction-id]
  (let [maps (get-maps interaction-id)
        title (if (:is-disabled (first maps))
                (str ":checkered_flag: **WINNER:** " (calculate-winner-map interaction-id))
                ":triangular_flag_on_post: **VOTE FOR THE MAP PLEASE**, **1 MINUTE TO VOTE**")
        users-list (create-users-list maps)]
    (str title "\n" users-list)))

(defn format-maps [maps]
  (map (fn [map-item]
         {:map-name (map-item "map")
          :voted-users []
          :is-disabled false}) maps))

(defn convert-users-to-string [users]
  (reduce (fn [acc user]
            (str acc "<@" (.. user -user -id) ">")) "" users))

(defn wrong-vote-reply [interaction callee-voice-channel-id username]
  (go (try (<p! (.reply interaction #js {:content
                                     (str "This is the voting of <#"
                                          callee-voice-channel-id "> voice channel, " username)
                                     :ephemeral true}))
           (catch js/Error e (println "ERROR wrong-vote-reply go" e)))))

(defn find-user-in-maps [interaction-id user-id]
  (reduce (fn [acc map-item]
            (let [user-found (->> (:voted-users map-item)
                                  (filter #(= (:user-id %) user-id))
                                  (first))]
              (if user-found
                user-found
                acc))) nil (get-maps interaction-id)))

(defn save-user-in-maps! [interaction-id user-id username map-name]
  (swap! state update-in [:interactions interaction-id :maps]
                 #(map (fn [map-item] (if (= (:map-name map-item) map-name)
                                      (update map-item :voted-users conj {:user-id user-id
                                                                          :username username
                                                                          :map-name map-name
                                                                          :timestamp (.now js/Date)
                                                                          }) map-item)) %)))

(defn create-reply [interaction-id]
  #js {:content (create-content interaction-id)
       :components (create-buttons interaction-id)})

(defn handle-collector-event-type-button! [event]
  (let [event-interaction-id (.. event -message -interaction -id)
        map-name (.-customId event)
        user-id (.. event -user -id)
        username (.. event -user -username)
        voice-channel (.. event -member -voice -channel)
        callee-voice-channel-id (get-in @state
                                    [:interactions event-interaction-id :callee-voice-channel-id])]
    (if voice-channel
      (if (= callee-voice-channel-id (.-id voice-channel))
        (let [user-found (find-user-in-maps event-interaction-id user-id)]
          (go (try
                (if user-found
                  (<p! (.reply event #js {:content (str "You have already voted for "
                                                        (:map-name user-found) ", " username)
                                          :ephemeral true}))
                  (do
                    (save-user-in-maps! event-interaction-id user-id username map-name)
                    (<p! (.update event (create-reply event-interaction-id)))
                    (when (:is-disabled (first (get-maps event-interaction-id)))
                      (println "RE-REQUEST: update content and buttons")
                      (<p! (.editReply event (create-reply event-interaction-id))))
                    ))
                (catch js/Error e (println "ERROR handle-collector-event-type-button! go" e)))))
        (wrong-vote-reply event callee-voice-channel-id username))
      (wrong-vote-reply event callee-voice-channel-id username))))

(defn init-interaction! [interaction maps]
  (swap! state update :interactions assoc (.-id interaction) { :maps maps
                                                          :callee-voice-channel-id (.. interaction
                                                                                       -member
                                                                                       -voice
                                                                                       -channel
                                                                                       -id)}))

(defn get-users-in-voice [interaction]
  (convert-users-to-string (.from js/Array
                                  (.. interaction -member -voice -channel -members values))))

(defn interact! [interaction]
  (let [option (or (.. interaction -options (getString "mapmode")) "main")
        server-id (.. interaction -guild -id)
        interaction-id (.-id interaction)]
    (println "/go INTERACTION" (js/Date.))
    (go (try
          (let [maps
                (format-maps (db-utils/get-formatted-rows
                               (<p! (map-server/select-maps server-id option))))]
            (if (.. interaction -member -voice -channel)
              (do
                (js/setTimeout #(go (try
                                    (disable-buttons interaction-id)
                                    (<p! (.editReply interaction (create-reply interaction-id)))
                                    (js/setTimeout
                                      (fn []
                                        (swap! state update :interactions dissoc
                                               interaction-id)) 5000)
                                    (catch js/Error e (println ERROR-MESSAGE-INTERACT e)))) 60000)
                (init-interaction! interaction maps)
                (<p! (.reply interaction (create-reply interaction-id)))
                (let [users-in-voice (get-users-in-voice interaction)]
                  (<p! (.followUp interaction users-in-voice))))
                (<p! (.reply
                       interaction #js {:content (str "You need to be in a voice "
                                                      "channel to run map poll, "
                                                      (.. interaction -member -user -username))
                                        :ephemeral true}))))
          (catch js/Error e (println ERROR-MESSAGE-INTERACT e))))))
