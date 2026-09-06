(ns animal-management.server
  (:require [ring.adapter.jetty :as jetty]
            [animal-management.http :as http]))

(defn start
  []
  (jetty/run-jetty http/app
                   {:port 3000
                    :join? false}))