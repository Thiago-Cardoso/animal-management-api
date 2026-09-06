(ns animal-management.db
  (:require [next.jdbc :as jdbc]))

(def db-spec
  {:dbtype "postgresql"
   :dbname "animal_management"
   :host "localhost"
   :port 5432
   :user "thiagocardoso"})