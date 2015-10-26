{:db
 {:subprotocol "mysql"
  :subname "//localhost:3306/dbname"
  :database "dbname"
  :user "root"
  :password ""}

 :html-auth
 {:password ""
  }


 ;
 ; Meta data about our tables that can't be stored
 ; in the database.
 ;
 :tables
 {
  "areas" {:display-field "title"}
  "area_facts" {:fields
				{"detail_text" {:note "Populate detail_text OR detail_value, but not both"}
				 "detail_value" {:note "Populate detail_text OR detail_value, but not both"}}}
  "facts" {:display-field "title"}
  "fact_categories" {:display-field "title"}
  "places" {:display-field "title"}
  "place_categories" {:display-field "title"}
  "place_facts" {:fields
				 {"detail_text" {:note "Populate detail_text OR detail_value, but not both"}
				  "detail_value" {:note "Populate detail_text OR detail_value, but not both"}}}
  "regions" {:display-field "title"}
  }


 }

