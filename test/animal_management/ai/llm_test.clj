(ns animal-management.ai.llm-test
  (:require [clojure.test :refer [deftest is]]
            [animal-management.ai.llm :as llm]
            [cheshire.core :as json]))

(deftest parse-response-test
  (let [response
        {:steps
         [{:signature "fake-signature"
           :type "thought"}

          {:content
           [{:text "Hello from Gemini"
             :type "text"}]
           :type "model_output"}]}]

    (is (= "Hello from Gemini"
           (llm/parse-response response)))))

(deftest build-request-body-test
  (let [body (llm/build-request-body
               "Say hello in one sentence.")]
    (is (= {:model "gemini-3.6-flash"
            :input "Say hello in one sentence."}
           body))))

(deftest build-request-json-test
  (let [body (llm/build-request-body
               "Say hello in one sentence.")
        json-body (json/generate-string body)]
    (is (= "{\"model\":\"gemini-3.6-flash\",\"input\":\"Say hello in one sentence.\"}"
           json-body))))

(deftest build-request-test
  (let [request (llm/build-request
                  "Say hello in one sentence."
                  "test-api-key")
        headers (.headers request)]
    (is (= "POST"
           (.method request)))

    (is (= "https://generativelanguage.googleapis.com/v1beta/interactions"
           (str (.uri request))))

    (is (= "test-api-key"
           (.orElse
             (.firstValue headers "x-goog-api-key")
             nil)))

    (is (= "application/json"
           (.orElse
             (.firstValue headers "content-type")
             nil)))

    (is (some? (.bodyPublisher request)))))

(deftest send-request-test
  (let [request (llm/build-request
                  "Say hello in one sentence."
                  "test-api-key")
        fake-response
        {:status 200
         :body (json/generate-string
                 {:steps
                  [{:content
                    [{:text "Hello from Gemini"
                      :type "text"}]
                    :type "model_output"}]})}]

    (with-redefs [llm/send-request
                  (fn [_request]
                    fake-response)]

      (is (= fake-response
             (llm/send-request request))))))

(deftest generate-test
  (let [fake-response
        {:status 200
         :body (json/generate-string
                 {:steps
                  [{:content
                    [{:text "Hello from Gemini"
                      :type "text"}]
                    :type "model_output"}]})}]

    (with-redefs [llm/build-request
                  (fn [_prompt _api-key]
                    :fake-request)

                  llm/send-request
                  (fn [_request]
                    fake-response)]

      (is (= "Hello from Gemini"
             (llm/generate
               "Say hello in one sentence."))))))

(deftest build-request-uses-configured-timeout-test
  (let [request (llm/build-request
                  "Say hello in one sentence."
                  "test-api-key")]
    (is (some? (.timeout request)))))

(deftest generate-requires-api-key-test
  (with-redefs [llm/build-request
                (fn [_prompt _api-key]
                  (throw
                    (ex-info
                      "GEMINI_API_KEY environment variable is required"
                      {})))]

    (is (thrown?
          clojure.lang.ExceptionInfo
          (llm/generate
            "Say hello in one sentence.")))))

(deftest send-request-rejects-http-error-test
  (let [fake-request :fake-request]

    (with-redefs [llm/send-request
                  (fn [_request]
                    (throw
                      (ex-info
                        "Gemini API request failed"
                        {:status 401
                         :body "{\"error\":{\"message\":\"Invalid API key\"}}"})))]

      (is (thrown?
            clojure.lang.ExceptionInfo
            (llm/send-request fake-request))))))