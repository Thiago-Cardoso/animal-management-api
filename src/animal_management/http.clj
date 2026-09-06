(ns animal-management.http
  (:require [reitit.ring :as ring]
            [cheshire.core :as json]))

(defn health-handler
  [_request]
  {:status 200
   :headers {"Content-Type" "text/plain"}
   :body "OK"})

(def animals
  [{:name "Luna"
    :species :dog
    :status :available}])

(defn animals-handler
  [_request]
  {:status 200
   :headers {"Content-Type" "application/json"}
   :body (json/generate-string animals)})

(def app
  (ring/ring-handler
    (ring/router
      [["/health" {:get health-handler}]
       ["/animals" {:get animals-handler}]])))