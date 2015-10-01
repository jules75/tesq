(ns tesq.core
  (:require [tesq.config :refer [DB]]
			[clojure.java.jdbc :as jdbc]
			[clojure.string :refer [replace capitalize]]
			[compojure.core :refer [defroutes GET POST]]
			[compojure.route :refer [resources not-found]]
			[compojure.handler :refer [site]]
			[hiccup.core :as h]
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


(defn table->html
  "Given table name, fetch all rows from database and return
  as HTML table."
  [table]
  (let [rows (jdbc/query DB [(str "SELECT * FROM " table)])]
	(h/html
	 [:table
	  [:thead (for [[k v] (first rows)] [:td k])]
	  (for [row rows]
		[:tr
		 (for [[k v] row] [:td (truncate (str v))])
		 ]
		)
	  ])))


(defn list-tables
  "Returns list of table names for current db."
  []
  (map last (map first (vec (show-tables DB)))))


(defn list-fields
  "Returns list of fields for given table.
  TODO: do this without string concat"
  [table]
  (jdbc/query DB [(str "DESC " table)]))


(defn prettify
  "Turn table name into something more human friendly."
  [s]
  (-> s capitalize (replace #"_" " ")))


(e/deftemplate main-template "html/_layout.html"
  [table]
  [:nav :ul :li] (e/clone-for
				  [item (list-tables)]
				  [:li :a] (e/content (prettify item))
				  [:li :a] (e/set-attr :href (str "/table/" item))
				  [:li] (e/add-class (if (= table item)"active"))
				  )
  [:#content] (e/html-content (table->html table)))


(defroutes routes
  (GET "/table/:table" [table] (main-template table))
  (resources "/")
  (not-found "Page not found"))


(def app (wrap-gzip (site routes)))

