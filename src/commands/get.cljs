(ns commands.get
  (:require ["discord.js" :as discord]))

(def builder
  (.. (discord/SlashCommandBuilder.)
      (setName "get")
      (setDescription "Get user stats")
      (addUserOption (fn [option]
                         (.. option
                             (setName "user")
                             (setDescription "Select user"))))
      toJSON))

