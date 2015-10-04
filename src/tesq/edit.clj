(ns tesq.edit
  "Edit table record"
  (:require [hiccup.core :refer [html]]
			[hiccup.util :refer [escape-html]]
			[hiccup.form :refer [form-to text-field text-area submit-button label hidden-field]]
			))


(def big-text 100) ; anything bigger justifies textarea


(defn- read-only?
  "Returns true if field is NOT allowed to be edited. For now, fields
  called 'id' or ending in '_id' may not be edited."
  [fieldname]
  (or (= "id" fieldname) (re-find #"_id$" fieldname)))


(defn row->html
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

