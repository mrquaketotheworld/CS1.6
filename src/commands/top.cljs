(ns commands.top
  (:require ["discord.js" :as discord]))

(def builder
  (.. (discord/SlashCommandBuilder.)
      (setName "top")
      (setDescription "Show top 10")
      toJSON))

