(ns animal-management.ai.llm
  (:require [cheshire.core :as json])
  (:import [java.net URI]
           [java.net.http HttpClient
                          HttpRequest
                          HttpRequest$BodyPublishers
                          HttpResponse$BodyHandlers]
           [java.time Duration]))

(def gemini-url
  "https://generativelanguage.googleapis.com/v1beta/interactions")

(def gemini-model
  "gemini-3.6-flash")

(def request-timeout-seconds
  120)

(def connect-timeout-seconds
  10)

(defn build-request-body
  [prompt]
  {:model gemini-model
   :input prompt})

(defn build-request
  [prompt api-key]
  (-> (HttpRequest/newBuilder)
      (.uri (URI/create gemini-url))
      (.timeout
        (Duration/ofSeconds request-timeout-seconds))
      (.header "x-goog-api-key" api-key)
      (.header "content-type" "application/json")
      (.POST
        (HttpRequest$BodyPublishers/ofString
          (json/generate-string
            (build-request-body prompt))))
      (.build)))

(defn parse-response
  [response]
  (let [model-output
        (some #(when (= "model_output" (:type %)) %)
              (:steps response))]
    (get-in model-output [:content 0 :text])))

(defn send-request
  [request]
  (let [client (-> (HttpClient/newBuilder)
                   (.connectTimeout
                     (Duration/ofSeconds connect-timeout-seconds))
                   (.build))
        response (.send
                   client
                   request
                   (HttpResponse$BodyHandlers/ofString))
        status (.statusCode response)
        body (.body response)]

    (if (<= 200 status 299)
      {:status status
       :body body}
      (throw
        (ex-info
          "Gemini API request failed"
          {:status status
           :body body})))))

(defn generate
  [prompt]
  (let [api-key (System/getenv "GEMINI_API_KEY")]

    (when (nil? api-key)
      (throw
        (ex-info
          "GEMINI_API_KEY environment variable is required"
          {})))

    (let [request (build-request prompt api-key)
          response (send-request request)]
      (parse-response
        (json/parse-string (:body response) true)))))