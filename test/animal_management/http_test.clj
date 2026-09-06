(ns animal-management.http-test
  (:require [clojure.test :refer [deftest is]]
            [animal-management.http :as http]
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