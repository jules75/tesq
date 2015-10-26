(ns tesq.utils
  (:require	[clojure.string :refer [replace capitalize]]))


(defn prettify
  "Turn table name into something more human friendly."
  [s]
  (-> s capitalize (replace #"_" " ")))


(defn singularise
  "Turn plural string into singular."
  [s]
  (cond

   (re-find #"ies$" s)
   (replace s #"ies$" "y")

   :else
   (replace s #"s$" "")

   ))

