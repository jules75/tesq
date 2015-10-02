(ns tesq.core
  (:require [tesq.config :refer [DB display-fields]]
			[clojure.java.jdbc :as jdbc]
			[clojure.pprint :refer [pprint]]
			[clojure.string :refer [replace capitalize trimr]]
			[clojure.set :refer [difference]]
			[compojure.core :refer [defroutes GET POST]]
			[compojure.route :refer [resources not-found]]
			[compojure.handler :refer [site]]
			[hiccup.core :as h]
			[hiccup.util :refer [escape-html]]
			[ring.middleware.gzip :refer [wrap-gzip]]
			[net.cgrand.enlive-html :as e]
			[net.cgrand.reload :refer [auto-reload]]
			[yesql.core :refer [defqueries]]
			))


(defqueries "sql/queries.sql")

(auto-reload *ns*)


(defn truncate
  "If string is too long, cut short and append ellipsis."
  [s]
  (let [limit 250]
	(if (< limit (count s))
	  (str (apply str (take limit s)) "... (more)")
	  s)))


(defn trim-comma
  "Remove comma from end of string (if present)."
  [s]
  (if (= \, (last s)) (apply str (butlast s)) s))


(defn backtick
  "Wrap string in backticks (`)."
  [s]
  (str "`" s "`"))


(defn build-query
  "Given table name, return query string that retrieves table data and
  looks up display fields from related tables."
  [table]
  (let [constraints (filter #(= table (:table_name %)) (fk-constraints DB (:database DB)))
		all-columns (map :field (jdbc/query DB [(str "DESC " table)]))
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


(defn table->html
  "Given table name, fetch all rows from database and return
  as HTML table."
  [table]
  (let [rows (jdbc/query DB [(build-query table)])]
	(h/html
	 [:p {:class "count"} (str (count rows) " rows found")]
	 [:table
	  [:thead (for [[k v] (first rows)] [:td (escape-html k)])]
	  (for [row rows]
		[:tr
		 (for [[k v] row] [:td (escape-html (truncate (str v)))])
		 ]
		)
	  ])))


(defn list-tables
  "Returns list of table names for current db."
  []
  (map last (map first (vec (show-tables DB)))))


(defn prettify
  "Turn table name into something more human friendly."
  [s]
  (-> s capitalize (replace #"_" " ")))


(e/deftemplate main-template "html/_layout.html"
  [table]
  [:nav :ul :li] (e/clone-for
				  [item (list-tables)]
				  [:li :a] (e/content (prettify item))
				  [:li :a] (e/set-attr :href (str "/view/" item))
				  [:li] (e/add-class (if (= table item)"active"))
				  )
  [:#content] (e/html-content (table->html table)))



(defroutes routes
  (GET "/view/:table" [table] (main-template table))
  (resources "/")
  (not-found "Page not found"))


(def app (wrap-gzip (site routes)))

