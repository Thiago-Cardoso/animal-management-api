(ns animal-management.core)

(defn create-animal
  [name species]
  {:name name
   :species species
   :status :available})

(defn rename-animal
  [animal new-name]
  (assoc animal :name new-name))

(defn animal-name
  [animal]
  (:name animal))

(defn adopt-animal
  [animal]
  (if (= :available (:status animal))
    (assoc animal :status :adopted)
    animal))