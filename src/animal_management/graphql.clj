(ns animal-management.graphql
  (:require [com.walmartlabs.lacinia :as lacinia]
            [com.walmartlabs.lacinia.schema :as schema]
            [cheshire.core :as json]
            [animal-management.repository :as repository]
            [clojure.string :as string]))

(defn- domain-animal->graphql
  [animal]
  (-> animal
      (update :species #(keyword (string/upper-case (name %))))
      (update :status #(keyword (string/upper-case (name %))))))

(def graphql-schema
  (schema/compile
    {:enums
     {:Species
      {:values [:DOG :CAT]}

      :AnimalStatus
      {:values [:AVAILABLE :ADOPTED]}}

     :objects
     {:Animal
      {:fields
       {:id
        {:type 'Int}

        :name
        {:type 'String}

        :species
        {:type 'Species}

        :status
        {:type 'AnimalStatus}}}}

     :queries
     {:animals
      {:type '(list :Animal)
       :resolve
       (fn [_context _args _value]
         (map domain-animal->graphql
              (repository/list-animals)))}

      :hello
      {:type 'String
       :resolve (constantly "Hello from Animal Management API")}}}))

(defn graphql-handler
  [request]
  (let [query (get-in request [:body :query])
        result (lacinia/execute graphql-schema query nil nil)]
    {:status 200
     :headers {"Content-Type" "application/json"}
     :body (json/generate-string result)}))