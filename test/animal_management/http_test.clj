(ns animal-management.http-test
  (:require [clojure.test :refer [deftest is]]
            [animal-management.http :as http]))

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