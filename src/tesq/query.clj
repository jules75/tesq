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


(defn- squote
  "Wrap string in single quotes."
  [s]
  (str "'" s "'"))


(defn- numeric-string?
  "True if string looks numeric (int, double, negative)."
  [s]
  (boolean (re-find #"^-?\d+\.?\d*$" s)))


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


(defn- nil-string
  "If s is empty return nil, otherwise s."
  [s]
  (if (empty? s) nil s))


(defn- mapvals
  "Map f over vals in map, preserve keys."
  [f m]
  (into {} (for [[k v] m] [k (f v)])))


(defn update-record
  "Params is map of key/val field/value pairs, all strings.
  Must contain 'table' and 'id' keys."
  [params]
  (let [table (:table params)
		id (:id params)
		fields (mapvals nil-string (dissoc params :table :id))]
	(str "UPDATE " table " SET "
		 (trim-comma
		  (reduce str
				  (for [[k v] fields]
					(str
					 (backtick (name k)) "="
					 (cond
					  (empty? v) "NULL"
					  (numeric-string? v) v
					  :else (squote v))
					 ","))))
		 " WHERE id=" id ";")))

