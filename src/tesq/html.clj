(ns tesq.html
  "Produce HTML"
  (:require [tesq.utils :refer [prettify]]
			[hiccup.core :refer [html]]
			[hiccup.util :refer [escape-html]]
			[hiccup.form :refer [form-to text-field text-area submit-button label hidden-field]]
			[clojure.string :refer [trimr]]
			))


(def big-text 100) ; anything bigger justifies textarea


(defn- read-only?
  "Returns true if field is NOT allowed to be edited. For now, fields
  called 'id' or ending in '_id' may not be edited."
  [fieldname]
  (or (= "id" fieldname) (re-find #"_id$" fieldname)))


(defn- truncate
  "If string is too long, cut short and append ellipsis."
  [s]
  (let [limit 250]
	(if (< limit (count s))
	  (str (apply str (take limit s)) "... (more)")
	  s)))


(defn rows->table
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


(defn row->table
  "Given single db row, return HTML table."
  [row table]
  (html
   [:table
	(for [[k v] row]
	  [:tr
	   [:td k]
	   [:td v]])]
   [:button [:a {:href (str "/edit/" table "/" (:id row))} "Edit"]]
   ))


(defn render-count
  "Render db row containing count data (:count, :tablename)."
  [row table]
  (html [:p (str "Contains " (:count row) " " (prettify (:tablename row)))]))


(defn row->form
  "Given db row, return html form."
  [row table field-notes]
  (html
   (form-to
	[:post "/save"]
	(hidden-field :table table)
	(hidden-field :id (:id row))
	(interleave

	 (for [[k v] row]
	   [:p {:class "note"} (get-in field-notes [table (name k)])])

	 (for [[k v] row]
	   (label k k))

	 (for [[k v] row]
	   ((if (< big-text (count (str v))) text-area text-field)
		(if (read-only? (name k)) {:disabled "disabled"} {})
		k v))

	 )
	(submit-button "Save")
	)))

