(ns animal-management.repository
  (:require [next.jdbc :as jdbc]
            [next.jdbc.result-set :as rs]
            [animal-management.db :as db]))

(defn create-animal!
  [animal]
  (jdbc/execute-one!
    db/db-spec
    ["INSERT INTO animals (name, species, status)
      VALUES (?, ?, ?)
      RETURNING id, name, species, status"
     (:name animal)
     (name (:species animal))
     (name (:status animal))]
    {:builder-fn rs/as-unqualified-maps}))

(defn delete-animal!
  [id]
  (jdbc/execute-one!
    db/db-spec
    ["DELETE FROM animals WHERE id = ?"
     id]))