(ns tesq.query
  "Build SQL queries"
  (:require [clojure.set :refer [difference]]
			[clojure.string :refer [trimr]]
			))


(defn- trim-comma
  "Remove comma from end of string (if present)."
  [s]
  (if (= \, (last s)) (apply str (butlast s)) s))


(defn- backtick
  "Wrap string in backticks (`)."
  [s]
  (str "`" s "`"))


(defn select-all
  "Returns query string that retrieves table data and
  injects up display fields from related tables."
  [
   ; table name
   table

   ;
   ; foreign key contraints from schema, e.g.;
   ;
   ;({:constraint_name "area_facts_ibfk_1",
   ;  :table_name "area_facts",
   ;  :column_name "fact_id",
   ;  :referenced_table_name "facts",
   ;  :referenced_column_name "id"}
   ; {:constraint_name "area_facts_ibfk_2",
   ;  :table_name "area_facts",
   ;  :column_name "area_id",
   ;  :referenced_table_name "areas",
   ;  :referenced_column_name "id"})
   ;
   fk-constraints

   ; all column names in this table
   column-names

   ; map of display fields, key is table name, value is
   ; column name that can act as display field
   display-fields
   ]
  (let [constraints (filter #(= table (:table_name %)) fk-constraints)

		columns (difference (set column-names) (set (map :column_name constraints)))

		related-tables (for [{ftab :referenced_table_name
							  col :column_name} constraints]
						 (str ftab "." (backtick (get display-fields ftab))
							  " AS " col "_lookup, "))

		joins (for [{ftab :referenced_table_name
					 fcol :referenced_column_name
					 tab :table_name
					 col :column_name} constraints]
				(str " INNER JOIN " ftab " ON " tab "." col "=" ftab "." fcol))

		select-list (concat
					 (map #(str table "." (backtick %) ", ") columns)
					 related-tables)
		]
	(str "SELECT " (->> select-list (apply str) trimr trim-comma) " FROM " table (apply str joins))
	))


(defn select-one
  "Assumes primary key is 'id'."
  [table id]
  (str "SELECT * FROM " table " WHERE id=" id))



