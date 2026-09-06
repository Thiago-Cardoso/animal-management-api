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