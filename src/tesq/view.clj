(ns tesq.view
  "View table contents"
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


(defn table->html
  "Given db rows, return HTML table."
  [rows table]
  (html
   [:p {:class "count"} (str (count rows) " rows found")]
   [:table
	[:thead
	 (conj
	  (for [[k v] (first rows)] [:td (escape-html k)])
	  [:td "actions"])
	 ]
	(for [row rows]
	  [:tr
	   (conj
		(for [[k v] row] [:td (escape-html (truncate (str v)))])
		[:td [:a {:href (str "/edit/" table "/" (:id row))} "edit"]])
	   ]
	  )
	]))

