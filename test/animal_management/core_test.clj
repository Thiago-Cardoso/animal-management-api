(ns animal-management.core-test
  (:require [clojure.test :refer [deftest is]]
            [animal-management.core :as core]))

(deftest create-animal-test
  (is (= {:name "Luna"
          :species :dog}
         (core/create-animal "Luna" :dog))))

(deftest rename-animal-test
  (let [luna {:name "Luna"
              :species :dog}
        bella (core/rename-animal luna "Bella")]
    (is (= {:name "Bella"
            :species :dog}
           bella))
    (is (= {:name "Luna"
            :species :dog}
           luna))))

(deftest adopt-animal-test
  (let [luna {:name "Luna"
              :species :dog
              :status :available}
        adopted (core/adopt-animal luna)]
    (is (= :adopted (:status adopted)))
    (is (= :available (:status luna)))))

(deftest create-animal-test
  (is (= {:name "Luna"
          :species :dog
          :status :available}
         (core/create-animal "Luna" :dog))))

(deftest adopt-already-adopted-animal-test
  (let [luna {:name "Luna"
              :species :dog
              :status :adopted}
        result (core/adopt-animal luna)]
    (is (= :adopted (:status result)))))