(ns tesq.core
  (:require [tesq.config :refer [DB display-fields]]
			[tesq.view :refer [table->html build-query]]
			[clojure.java.jdbc :as jdbc]
			[clojure.string :refer [replace capitalize]]
			[compojure.core :refer [defroutes GET POST]]
			[compojure.route :refer [resources not-found]]
			[compojure.handler :refer [site]]
			[ring.middleware.gzip :refer [wrap-gzip]]
			[net.cgrand.enlive-html :as e]
			[net.cgrand.reload :refer [auto-reload]]
			[yesql.core :refer [defqueries]]
			))


(defqueries "sql/queries.sql")

(auto-reload *ns*)


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
				  [:li] (e/add-class (if (= table item)"active")))
  [:#content] (let [constraints (fk-constraints DB (:database DB))
					columns (map :field (jdbc/query DB [(str "DESC " table)]))
					query (build-query table constraints columns display-fields)]
				(e/html-content (table->html (jdbc/query DB [query])))
				))


(defroutes routes
  (GET "/view/:table" [table] (main-template table))
  (resources "/")
  (not-found "Page not found"))


(def app (wrap-gzip (site routes)))

