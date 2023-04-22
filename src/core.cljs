(ns core
  (:require [commands.deploy-commands :as commands]))


(defn main [& args]
  (commands/register-commands)
  (println "hello world") )
