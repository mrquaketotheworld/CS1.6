(ns commands.set
  (:require ["discord.js" :as discord]))

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

