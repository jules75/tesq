(ns tesq.edit
  "Edit table record"
  (:require [hiccup.core :refer [html]]
			[hiccup.util :refer [escape-html]]
			[hiccup.form :refer [form-to text-field submit-button label]]
			))

(defn row->html
  "Given db row, return html form."
  [row]
  (html
   (form-to
	[:put "/post"]
	(interleave
	 (for [[k v] row] (label k k))
	 (repeat [:br])
	 (for [[k v] row] (text-field k v))
	 (repeat [:br])
	 (repeat [:br])
	 )
	(submit-button "Save")
	)))

