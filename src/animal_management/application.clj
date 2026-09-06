(ns animal-management.application
  (:require [animal-management.core :as core]
            [animal-management.repository :as repository]))

(defn create-animal
  [animal]
  (repository/create-animal! animal))

(defn find-animal
  [id]
  (repository/find-animal id))

(defn list-animals
  []
  (repository/list-animals))

(defn update-animal
  [id animal]
  (repository/update-animal! id animal))

(defn adopt-animal
  [id]
  (let [animal (repository/find-animal id)]
    (when animal
      (let [adopted-animal (core/adopt-animal animal)]
        (repository/update-animal! id adopted-animal)))))

(defn delete-animal
  [id]
  (repository/delete-animal! id))
