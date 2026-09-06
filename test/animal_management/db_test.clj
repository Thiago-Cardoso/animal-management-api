(ns animal-management.db-test
  (:require [clojure.test :refer [deftest is]]
            [next.jdbc :as jdbc]
            [animal-management.db :as db]))

(deftest database-connection-test
  (let [result (jdbc/execute-one!
                 db/db-spec
                 ["SELECT 1 AS result"])]
    (is (= 1 (:result result)))))