(ns animal-management.http
  (:require [reitit.ring :as ring]
            [ring.middleware.json :refer [wrap-json-body]]
            [cheshire.core :as json]
            [animal-management.graphql :as graphql]))

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
  (wrap-json-body
    (ring/ring-handler
      (ring/router
        [["/health" {:get health-handler}]
         ["/animals" {:get animals-handler}]
         ["/graphql" {:post graphql/graphql-handler}]]))
    {:keywords? true}))