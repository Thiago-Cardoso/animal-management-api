(ns animal-management.http
  (:require [reitit.ring :as ring]))

(defn health-handler
  [_request]
  {:status 200
   :headers {"Content-Type" "text/plain"}
   :body "OK"})

(def app
  (ring/ring-handler
    (ring/router
      [["/health" {:get health-handler}]])))