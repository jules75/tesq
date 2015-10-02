(ns tesq.view
  "View table contents"
  (:require [hiccup.core :refer [html]]
			[hiccup.util :refer [escape-html]]
			[clojure.set :refer [difference]]
			[clojure.string :refer [trimr]]
			))


(defn- truncate
  "If string is too long, cut short and append ellipsis."
  [s]
  (let [limit 250]
	(if (< limit (count s))
	  (str (apply str (take limit s)) "... (more)")
	  s)))


(defn- trim-comma
  "Remove comma from end of string (if present)."
  [s]
  (if (= \, (last s)) (apply str (butlast s)) s))


(defn- backtick
  "Wrap string in backticks (`)."
  [s]
  (str "`" s "`"))


(defn table->html
  "Given db rows, return HTML table."
  [rows]
  (html
   [:p {:class "count"} (str (count rows) " rows found")]
   [:table
	[:thead (for [[k v] (first rows)] [:td (escape-html k)])]
	(for [row rows]
	  [:tr
	   (for [[k v] row] [:td (escape-html (truncate (str v)))])
	   ]
	  )
	]))


(defn build-query
  "Given table name, return query string that retrieves table data and
  looks up display fields from related tables."
  [table all-constraints all-columns display-fields]
  (let [constraints (filter #(= table (:table_name %)) all-constraints)
		non-lookup-columns (difference (set all-columns) (set (map :column_name constraints)))
		related-tables (for [{:keys [referenced_table_name column_name]} constraints]
						 (str referenced_table_name "."
							  (backtick (get display-fields referenced_table_name))
							  " AS " column_name "_lookup, "))
		joins (for [{:keys [referenced_table_name referenced_column_name table_name column_name]} constraints]
				(str " INNER JOIN " referenced_table_name " ON " table_name "." column_name
					 "=" referenced_table_name "." referenced_column_name))
		table-clause (apply str
							(concat
							 (map #(str table "." (backtick %) ", ") non-lookup-columns)
							 related-tables))]
	(str "SELECT " (trim-comma (trimr table-clause)) " FROM " table (apply str joins))
	))

