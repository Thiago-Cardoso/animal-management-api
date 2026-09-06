(ns animal-management.http-test
  (:require [clojure.test :refer [deftest is]]
            [animal-management.http :as http]
            [animal-management.repository :as repository]
            [cheshire.core :as json]))

(deftest health-handler-test
  (let [response (http/health-handler {})]
    (is (= 200 (:status response)))
    (is (= "text/plain" (get-in response [:headers "Content-Type"])))
    (is (= "OK" (:body response)))))

(deftest health-route-test
  (let [response (http/app
                   {:request-method :get
                    :uri "/health"})]
    (is (= 200 (:status response)))
    (is (= "text/plain"
           (get-in response [:headers "Content-Type"])))
    (is (= "OK" (:body response)))))

(deftest animals-route-test
  (let [response (http/app
                   {:request-method :get
                    :uri "/animals"})]
    (is (= 200 (:status response)))
    (is (= "application/json"
           (get-in response [:headers "Content-Type"])))
    (is (= "[{\"name\":\"Luna\",\"species\":\"dog\",\"status\":\"available\"}]"
           (:body response)))))

(deftest json-generation-test
  (let [animals [{:name "Luna"
                  :species :dog
                  :status :available}]
        json-string (json/generate-string animals)]
    (is (= "[{\"name\":\"Luna\",\"species\":\"dog\",\"status\":\"available\"}]"
           json-string))))

(deftest graphql-route-test
  (let [response (http/app
                   {:request-method :post
                    :uri "/graphql"
                    :headers {"content-type" "application/json"}
                    :body (java.io.ByteArrayInputStream.
                            (.getBytes "{\"query\":\"{ hello }\"}"))})
        body (json/parse-string (:body response) true)]
    (is (= 200 (:status response)))
    (is (= "application/json"
           (get-in response [:headers "Content-Type"])))
    (is (= "Hello from Animal Management API"
           (get-in body [:data :hello])))))

(deftest graphql-animals-route-test
  (let [animal (repository/create-animal!
                 {:name "Thor"
                  :species :dog
                  :status :available})
        response (http/app
                   {:request-method :post
                    :uri "/graphql"
                    :headers {"content-type" "application/json"}
                    :body (java.io.ByteArrayInputStream.
                            (.getBytes
                              "{\"query\":\"{ animals { id name species status } }\"}"))})
        body (json/parse-string (:body response) true)
        animals (get-in body [:data :animals])
        found (first (filter #(= (:id %) (:id animal)) animals))]
    (is (= 200 (:status response)))
    (is (= "application/json"
           (get-in response [:headers "Content-Type"])))
    (is (some? found))
    (is (= "Thor" (:name found)))
    (is (= "DOG" (:species found)))
    (is (= "AVAILABLE" (:status found)))

    (repository/delete-animal! (:id animal))))