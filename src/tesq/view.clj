(ns tesq.view
  "Edit table record"
  (:require [hiccup.core :refer [html]]
			[hiccup.util :refer [escape-html]]
			))


(defn row->html
  "Given db row, return html table."
  [row table]
  (html
   [:table
	(for [[k v] row]
	  [:tr
	   [:td k]
	   [:td v]])]
   [:p [:a {:href (str "/edit/" table "/" (:id row))} "edit"]]
   ))

