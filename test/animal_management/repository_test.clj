(ns animal-management.repository-test
  (:require [clojure.test :refer [deftest is]]
            [animal-management.repository :as repository]))

(deftest create-animal-test
  (let [animal (repository/create-animal!
                 {:name "Luna"
                  :species :dog
                  :status :available})]
    (is (number? (:id animal)))
    (is (= "Luna" (:name animal)))
    (is (= "dog" (:species animal)))
    (is (= "available" (:status animal)))

    (repository/delete-animal! (:id animal))))

(deftest find-animal-test
  (let [created (repository/create-animal!
                  {:name "Luna"
                   :species :dog
                   :status :available})
        found (repository/find-animal (:id created))]
    (is (= (:id created) (:id found)))
    (is (= "Luna" (:name found)))
    (is (= :dog (:species found)))
    (is (= :available (:status found)))

    (repository/delete-animal! (:id created))))

(deftest find-nonexistent-animal-test
  (is (nil? (repository/find-animal 999999))))