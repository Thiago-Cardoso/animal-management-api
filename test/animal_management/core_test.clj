(ns animal-management.core-test
  (:require [clojure.test :refer [deftest is]]
            [animal-management.core :as core]))

(deftest create-animal-test
  (is (= {:name "Luna"
          :species :dog}
         (core/create-animal "Luna" :dog))))