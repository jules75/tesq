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
  "facts" {:display-field "title"}
  "fact_categories" {:display-field "title"}
  "places" {:display-field "title"}
  "place_categories" {:display-field "title"}
  "regions" {:display-field "title"}
  }


 ;
 ; Field notes are shown when user is editing a record.
 ; Key is table name, value is map of field/note pairs.
 ;
 :field-notes
 {"area_facts"
  {"detail_text" "Populate detail_text OR detail_value, but not both"
   "detail_value" "Populate detail_text OR detail_value, but not both"}
  "place_facts"
  {"detail_text" "Populate detail_text OR detail_value, but not both"
   "detail_value" "Populate detail_text OR detail_value, but not both"}
  }


 }

