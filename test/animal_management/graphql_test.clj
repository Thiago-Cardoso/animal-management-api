(ns animal-management.graphql-test
  (:require [clojure.test :refer [deftest is]]
            [animal-management.graphql :as graphql]
            [animal-management.repository :as repository]
            [com.walmartlabs.lacinia :as lacinia]
            [cheshire.core :as json]))

(deftest graphql-handler-test
  (let [response (graphql/graphql-handler
                   {:body
                    {:query "{ hello }"}})
        body (json/parse-string (:body response) true)]
    (is (= 200 (:status response)))
    (is (= "application/json"
           (get-in response [:headers "Content-Type"])))
    (is (= "Hello from Animal Management API"
           (get-in body [:data :hello])))))

(deftest animals-query-test
  (let [animal (repository/create-animal!
                 {:name "Milo"
                  :species :cat
                  :status :available})
        result (lacinia/execute
                 graphql/graphql-schema
                 "{ animals { id name species status } }"
                 nil
                 nil)
        animals (get-in result [:data :animals])
        found (first (filter #(= (:id %) (:id animal)) animals))]
    (is (some? found))
    (is (= "Milo" (:name found)))
    (is (= :CAT (:species found)))
    (is (= :AVAILABLE (:status found)))

    (repository/delete-animal! (:id animal))))