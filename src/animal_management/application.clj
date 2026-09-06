(ns animal-management.application
  (:require [animal-management.repository :as repository]))

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

(defn delete-animal
  [id]
  (repository/delete-animal! id))