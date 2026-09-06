(ns animal-management.application-test
  (:require [clojure.test :refer [deftest is]]
            [animal-management.application :as application]
            [animal-management.repository :as repository]))

(deftest create-animal-test
  (let [animal (application/create-animal
                 {:name "Charlie"
                  :species :dog
                  :status :available})]
    (is (number? (:id animal)))
    (is (= "Charlie" (:name animal)))
    (is (= :dog (:species animal)))
    (is (= :available (:status animal)))

    (repository/delete-animal! (:id animal))))

(deftest find-animal-test
  (let [created (application/create-animal
                  {:name "Bella"
                   :species :cat
                   :status :available})
        found (application/find-animal (:id created))]
    (is (= (:id created) (:id found)))
    (is (= "Bella" (:name found)))
    (is (= :cat (:species found)))
    (is (= :available (:status found)))

    (repository/delete-animal! (:id created))))

(deftest list-animals-test
  (let [first-animal (application/create-animal
                       {:name "Luna"
                        :species :dog
                        :status :available})
        second-animal (application/create-animal
                        {:name "Milo"
                         :species :cat
                         :status :available})
        animals (application/list-animals)]
    (is (= 2 (count animals)))
    (is (= ["Luna" "Milo"]
           (map :name animals)))
    (is (= [:dog :cat]
           (map :species animals)))
    (is (= [:available :available]
           (map :status animals)))

    (repository/delete-animal! (:id first-animal))
    (repository/delete-animal! (:id second-animal))))

(deftest update-animal-test
  (let [created (application/create-animal
                  {:name "Luna"
                   :species :dog
                   :status :available})
        updated (application/update-animal
                  (:id created)
                  {:name "Bella"
                   :species :cat
                   :status :adopted})]
    (is (= (:id created) (:id updated)))
    (is (= "Bella" (:name updated)))
    (is (= :cat (:species updated)))
    (is (= :adopted (:status updated)))

    (repository/delete-animal! (:id created))))

(deftest delete-animal-test
  (let [animal (application/create-animal
                 {:name "Rocky"
                  :species :dog
                  :status :available})]
    (application/delete-animal (:id animal))

    (is (nil? (application/find-animal (:id animal))))))