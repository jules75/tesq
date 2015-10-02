(ns tesq.edit
  "Edit table record"
  (:require [hiccup.core :refer [html]]
			[hiccup.util :refer [escape-html]]
			))


(defn build-query
  [table pk]
  (str "SELECT * FROM " table)
  )

