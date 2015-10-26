(ns tesq.core
  (:require [tesq.html :as html]
			[tesq.query :as q]
			[tesq.utils :refer [prettify]]
			[clojure.java.jdbc :as jdbc]
			[compojure.core :refer [defroutes GET POST]]
			[compojure.route :refer [resources not-found]]
			[compojure.handler :refer [site]]
			[ring.middleware.gzip :refer [wrap-gzip]]
			[ring.middleware.basic-authentication :refer [wrap-basic-authentication]]
			[net.cgrand.enlive-html :as e]
			[net.cgrand.reload :refer [auto-reload]]
			[yesql.core :refer [defqueries]]
			))


(defqueries "sql/queries.sql")

(auto-reload *ns*)


(def config
  (->>
   (slurp "config.clj")
   read-string
   (into {}) ; ensure no code execution
   ))


(defn- auth?
  "For basic HTML authentication"
  [username password]
  (= password (-> config :html-auth :password)))


(def DB (:db config))


(defn- list-tables
  "Returns list of table names for current db."
  []
  (map last (map first (vec (show-tables DB)))))


(e/deftemplate list-template "html/_layout.html"
  [table field value]
  [:nav :ul :li] (e/clone-for
				  [item (list-tables)]
				  [:li :a] (e/content (prettify item))
				  [:li :a] (e/set-attr :href (str "/list?table=" item))
				  [:li] (e/add-class (if (= table item)"active")))
  [:#content] (let [constraints (fk-constraints DB (:database DB))
					columns (map :field (jdbc/query DB [(str "DESC " table)]))
					sql (q/select-all
						 table
						 field
						 value
						 constraints
						 columns
						 (:display-fields config))]
				(e/html-content
				 (html/rows->table
				  (jdbc/query DB [sql])
				  table))
				))


(e/deftemplate edit-template "html/_layout.html"
  [table id]
  [:#content]
  (e/html-content
   (html/row->form
	(first (jdbc/query DB [(q/select-one table id)]))
	table
	(:field-notes config))))


(e/deftemplate view-template "html/_layout.html"
  [table id]
  [:#content]
  (e/html-content
   (let [row (first (jdbc/query DB [(q/select-one table id)]))]
	 (apply str
			(cons
			 (html/row->table row table)
			 (for [sql (q/count-children table id (fk-constraints DB (:database DB)))]
			   (html/render-count (first (jdbc/query DB [sql])) table id))
			 )))))


(defroutes routes
  (GET "/" [] (list-template (first (list-tables))))
  (GET "/list" [& {:keys [table field value]}] (list-template table field value))
  (GET "/view" [& {:keys [id table]}] (view-template table id))
  (GET "/edit" [& {:keys [id table]}] (edit-template table id))
  (POST "/save" {params :params}
		(do
		  (jdbc/execute! DB [(q/update-record params)])
		  (str "Saved. <a href=\"/\">Continue</a>")
		  ))
  (resources "/")
  (not-found "Page not found"))


(def app (wrap-gzip (wrap-basic-authentication (site routes) auth?)))

