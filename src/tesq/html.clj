(ns tesq.html
  "Produce HTML"
  (:require [hiccup.core :refer [html]]
			[hiccup.util :refer [escape-html]]
			[clojure.string :refer [trimr]]
			))


(defn- truncate
  "If string is too long, cut short and append ellipsis."
  [s]
  (let [limit 250]
	(if (< limit (count s))
	  (str (apply str (take limit s)) "... (more)")
	  s)))


(defn rows->html
  "Given many db rows, return HTML table."
  [rows table]
  (html
   [:p {:class "count"} (str (count rows) " rows found")]
   [:table
	[:thead
	 (conj
	  (for [[k v] (first rows)
			:when (not= :id k)]
		[:td (escape-html k)])
	  [:td "actions"])]
	(for [row rows]
	  [:tr
	   (conj
		(for [[k v] row
			  :when (not= :id k)]
		  [:td (escape-html (truncate (str v)))]
		  )
		[:td [:a {:href (str "/view/" table "/" (:id row))} "view"]]
		)])]))


(defn row->html
  "Given single db row, return HTML table."
  [row table]
  (html
   [:table
	(for [[k v] row]
	  [:tr
	   [:td k]
	   [:td v]])]
   [:p [:a {:href (str "/edit/" table "/" (:id row))} "edit"]]
   ))

