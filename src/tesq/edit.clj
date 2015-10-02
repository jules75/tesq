(ns tesq.edit
  "Edit table record"
  (:require [hiccup.core :refer [html]]
			[hiccup.util :refer [escape-html]]
			[hiccup.form :refer [form-to text-field submit-button label]]
			))


(defn read-only?
  "Returns true if field is NOT allowed to be edited. For now, fields
  called 'id' or ending in '_id' may not be edited."
  [fieldname]
  (or (= "id" fieldname) (re-find #"_id$" fieldname)))


(defn row->html
  "Given db row, return html form."
  [row]
  (html
   (form-to
	[:post "/save"]
	(interleave

	 (for [[k v] row]
	   (label k k))

	 (for [[k v] row]
	   (text-field (if (read-only? (name k)) {:disabled "disabled"} {}) k v))

	 )
	(submit-button "Save")
	)))

