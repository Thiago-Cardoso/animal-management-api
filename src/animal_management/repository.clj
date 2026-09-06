(ns animal-management.repository
  (:require [next.jdbc :as jdbc]
            [next.jdbc.result-set :as rs]
            [animal-management.db :as db]))

(defn- database-animal->domain
  [animal]
  (when animal
    (-> animal
        (update :species keyword)
        (update :status keyword))))

(defn create-animal!
  [animal]
  (some-> (jdbc/execute-one!
            db/db-spec
            ["INSERT INTO animals (name, species, status)
              VALUES (?, ?, ?)
              RETURNING id, name, species, status"
             (:name animal)
             (name (:species animal))
             (name (:status animal))]
            {:builder-fn rs/as-unqualified-maps})
          database-animal->domain))

(defn delete-animal!
  [id]
  (jdbc/execute-one!
    db/db-spec
    ["DELETE FROM animals WHERE id = ?"
     id]))

(defn find-animal
  [id]
  (some-> (jdbc/execute-one!
            db/db-spec
            ["SELECT id, name, species, status
              FROM animals
              WHERE id = ?"
             id]
            {:builder-fn rs/as-unqualified-maps})
          database-animal->domain))

(defn update-animal!
  [id animal]
  (some-> (jdbc/execute-one!
            db/db-spec
            ["UPDATE animals
              SET name = ?, species = ?, status = ?
              WHERE id = ?
              RETURNING id, name, species, status"
             (:name animal)
             (name (:species animal))
             (name (:status animal))
             id]
            {:builder-fn rs/as-unqualified-maps})
          database-animal->domain))

(defn list-animals
  []
  (map database-animal->domain
       (jdbc/execute!
         db/db-spec
         ["SELECT id, name, species, status
           FROM animals
           ORDER BY id"]
         {:builder-fn rs/as-unqualified-maps})))

