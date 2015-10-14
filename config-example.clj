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
 ; Key is table name, val is field name that represents display
 ; value for that field. Tables without an entry here are not
 ; considered to have a display field.
 ;
 :display-fields
 {"areas" "title"
  "facts" "title"
  "fact_categories" "title"
  "places" "title"
  "place_categories" "title"
  "regions" "title"}



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

