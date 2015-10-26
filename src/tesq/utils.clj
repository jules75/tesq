(ns tesq.utils
  (:require	[clojure.string :refer [replace capitalize]]))


(defn prettify
  "Turn table name into something more human friendly."
  [s]
  (-> s capitalize (replace #"_" " ")))


(defn singularise
  "Turn plural string into singular."
  [s]
  (replace s #"s$" ""))
