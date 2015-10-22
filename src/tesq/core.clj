(ns tesq.core
  (:require [tesq.view :as view]
			[tesq.edit :as edit]
			[tesq.query :as q]
			[clojure.java.jdbc :as jdbc]
			[clojure.string :refer [replace capitalize]]
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


(defn auth?
  "For basic HTML authentication"
  [username password]
  (= password (-> config :html-auth :password)))


(def DB (:db config))


(defn list-tables
  "Returns list of table names for current db."
  []
  (map last (map first (vec (show-tables DB)))))


(defn prettify
  "Turn table name into something more human friendly."
  [s]
  (-> s capitalize (replace #"_" " ")))


(e/deftemplate view-template "html/_layout.html"
  [table]
  [:nav :ul :li] (e/clone-for
				  [item (list-tables)]
				  [:li :a] (e/content (prettify item))
				  [:li :a] (e/set-attr :href (str "/list/" item))
				  [:li] (e/add-class (if (= table item)"active")))
  [:#content] (let [constraints (fk-constraints DB (:database DB))
					columns (map :field (jdbc/query DB [(str "DESC " table)]))
					sql (q/select-all table constraints columns (:display-fields config))]
				(e/html-content (view/table->html (jdbc/query DB [sql]) table))
				))


(e/deftemplate edit-template "html/_layout.html"
  [table id]
  [:#content]
  (e/html-content
   (edit/row->html
	(first (jdbc/query DB [(q/select-one table id)]))
	table
	(:field-notes config))))


(defroutes routes
  (GET "/" [] (view-template (first (list-tables))))
  (GET "/list/:table" [table] (view-template table))
  (GET "/edit/:table/:id" [table id] (edit-template table id))
  (POST "/save" {params :params}
		(do
		  (jdbc/execute! DB [(q/update-record params)])
		  "Saved. <a href=\"/\">Continue</a>"
		  ))
  (resources "/")
  (not-found "Page not found"))


(def app (wrap-gzip (wrap-basic-authentication (site routes) auth?)))
