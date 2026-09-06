(ns animal-management.migration
  (:require [migratus.core :as migratus]
            [animal-management.db :as db]))

(def config
  {:store :database
   :migration-dir "migrations"
   :db {:dbtype "postgresql"
        :dbname (:dbname db/db-spec)
        :host (:host db/db-spec)
        :port (:port db/db-spec)
        :user (:user db/db-spec)}})

(defn migrate
  []
  (migratus/migrate config))