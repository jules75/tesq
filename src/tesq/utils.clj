(ns tesq.utils
  (:require	[clojure.string :refer [replace capitalize]]
			))


(defn prettify
  "Turn table name into something more human friendly."
  [s]
  (-> s capitalize (replace #"_" " ")))

